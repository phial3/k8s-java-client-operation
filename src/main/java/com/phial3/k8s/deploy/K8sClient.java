package com.phial3.k8s.deploy;

import cn.hutool.core.util.StrUtil;
import com.phial3.k8s.config.AppConfig;
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
