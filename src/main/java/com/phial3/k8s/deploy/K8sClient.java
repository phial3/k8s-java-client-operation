package com.phial3.k8s.deploy;

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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class K8sClient {

    private ApiClient apiClient;
    private AppsV1Api appsV1Api;
    private CoreV1Api coreV1Api;

    @Resource
    private AppConfig appConfig;

    @PostConstruct
    public void init() {
        initK8sClient();
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
    public void initK8sClient() {
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

    public void loadLocalKubeConfig() throws IOException {
        //load local config
        String configPath = System.getProperty("user.dir") + File.separator + "kubeConfig" + File.separator;
        ApiClient apiClient = Config.fromConfig(Files.newInputStream(Paths.get(configPath + "config")));
        Configuration.setDefaultApiClient(apiClient);

        this.apiClient = apiClient;
        this.coreV1Api = new CoreV1Api(apiClient);
        this.appsV1Api = new AppsV1Api(apiClient);
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
}
