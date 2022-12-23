package org.phial3.k8s.deploy;

import cn.hutool.core.util.StrUtil;
import org.phial3.k8s.config.AppConfig;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Pod有以下几个状态:
 * Pending: 等待中
 * Pod已经被创建，但还没有完成调度，或者说有一个或多个镜像正处于从远程仓库下载的过程。
 * 处在这个阶段的Pod可能正在写数据到etcd中、调度、pull镜像或启动容器。
 * <p>
 * Running: 运行中
 * 该 Pod 已经绑定到了一个节点上，Pod 中所有的容器都已被创建。至少有一个容器正在运行，或者正处于启动或重启状态。
 * <p>
 * Succeeded:  正常终止
 * Pod中的所有的容器已经正常的执行后退出，并且不会自动重启，一般会是在部署job的时候会出现。
 * <p>
 * Failed: 异常停止
 * Pod 中的所有容器都已终止了，并且至少有一个容器是因为失败终止。也就是说，容器以非0状态退出或者被系统终止。
 * <p>
 * Unkonwn: 未知状态
 * API Server无法正常获取到Pod对象的状态信息，通常是由于其无法与所在工作节点的kubelet通信所致。
 */
@Slf4j
@Component
public class K8sClient {

    private ApiClient apiClient;
    private AppsV1Api appsV1Api;
    private CoreV1Api coreV1Api;

    @Resource
    private AppConfig appConfig;

    @Value("${k8s.server.enable-local}")
    private Boolean enableLocal;

    @PostConstruct
    public void init() {
        if (enableLocal) {
            initLocalK8sConfig();
        } else {
            initRemoteK8sConfig();
        }
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public AppsV1Api getAppsV1Api() {
        return appsV1Api;
    }

    public CoreV1Api getCoreV1Api() {
        return coreV1Api;
    }

    /**
     * 构建集群外通过UA访问的客户端
     * loading the out-of-cluster config, a kubeconfig from file-system
     */
    public void initRemoteK8sConfig() {
        try {
            StringReader strReader = new StringReader(appConfig.getKubConfigYaml());
            this.apiClient = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(strReader)).build();
            this.coreV1Api = new CoreV1Api(apiClient);
            this.appsV1Api = new AppsV1Api(apiClient);
            strReader.close();
        } catch (Exception e) {
            log.error("initK8sClient error!", e);
            throw new RuntimeException("initK8sClient error.");
        }
    }

    public void initLocalK8sConfig() {
        try {
            //load local config
            String kubeConfigPath = "/Users/admin/.kube/config";
            ApiClient apiClient = Config.fromConfig(kubeConfigPath);
            Configuration.setDefaultApiClient(apiClient);

            this.apiClient = apiClient;
            this.coreV1Api = new CoreV1Api(apiClient);
            this.appsV1Api = new AppsV1Api(apiClient);
        } catch (Exception e) {
            log.error("initLocalKubeConfig error.", e);
            throw new RuntimeException("initLocalKubeConfig error", e);
        }
    }


    public List<V1Pod> getPodList() {
        try {
            V1PodList list = coreV1Api.listNamespacedPod(appConfig.getK8sNamespace(), null, null, null, null, null, null, null, null, null, null);
            return list.getItems();
        } catch (ApiException e) {
            log.error("getPodList error " + e.getResponseBody(), e);
        }
        return null;
    }

    public String getAppServerLogs(int startDate,String namespace,String podName,String endLog){
        try {
            int now = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
            // 加 5 秒防止日志丢失
            int i =  now - startDate + 5;

            String logContent = coreV1Api.readNamespacedPodLog(podName, namespace, null, null, null, null, "true", null, i, null, null);

            // 去除重复日志
            if(StrUtil.isNotBlank(endLog)){
                int end = logContent.indexOf(endLog);
                if(end!=-1){
                    logContent = StrUtil.removePrefix(logContent.substring(end),endLog);
                }
            }
            return logContent;
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return "";
    }
}
