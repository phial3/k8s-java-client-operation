package com.phial3.k8s.deploy;

import com.phial3.k8s.config.AppConfig;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class K8sClient {
    /**
     * k8s-api客户端
     */
    private ApiClient apiClient;

    @Resource
    private AppConfig appConfig;

    @PostConstruct
    public void init() {
        initK8sClient();
    }

    /**
     * 构建集群POD内通过SA访问的客户端
     * loading the in-cluster config, including:
     * 1. @R_673_9260@ce-account CA
     * 2. @R_673_9260@ce-account bearer-token
     * 3. @R_673_9260@ce-account namespace
     * 4. master endpoints(ip, port) from pre-set environment variables
     */
//    public K8sClient() {
//        try {
//            this.apiClient = ClientBuilder.cluster().build();
//        } catch (IOException E) {
//            log.error("构建K8s-Client异常", E);
//            throw new RuntimeException("构建K8s-Client异常");
//        }
//    }

    /**
     * 构建集群外通过UA访问的客户端
     * loading the out-of-cluster config, a kubeconfig from file-system
     */
    public void initK8sClient() {
        try {
            String kubeConfig = appConfig.getKubConfigYaml();
            StringReader strReader = new StringReader(kubeConfig);
            this.apiClient = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(strReader)).build();
            strReader.close();
        } catch (IOException E) {
            log.error("读取kubeConfigPath异常", E);
            throw new RuntimeException("读取kubeConfigPath异常");
        } catch (Exception E) {
            log.error("构建K8s-Client异常", E);
            throw new RuntimeException("构建K8s-Client异常");
        }
    }

    /**
     * 获取所有的Pod
     *
     * @return podList
     */
    public V1PodList getAllPodList() {
        // new a CoreV1Api
        CoreV1Api api = new CoreV1Api(apiClient);
        // invokes the CoreV1Api client
        try {
            V1NamespaceList namespaceList = api.listNamespace(null, true, null, null, null, null, null, null, null, true);
            System.out.println(namespaceList);
            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
            return list;
        } catch (ApiException e) {
            log.error("获取podlist异常:" + e.getResponseBody(), e);
        }
        return null;
    }
}
