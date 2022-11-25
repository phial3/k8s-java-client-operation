package com.phial3.k8s.deploy;

import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1HTTPIngressPath;
import io.kubernetes.client.openapi.models.V1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1IngressServiceBackend;
import io.kubernetes.client.openapi.models.V1KeyToPath;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class DeployService {

    private final static String DEFAULT_NAMESPACE = "default";

    private final static String CONFIG_JSON = "config.json";

    private final static String LABEL_KEY = "app";

    private final static String CONFIG_MAP_PREFIX = "configmap-";

    private final static String CONFIG_JSON_VALUE = "{}";

    @Resource
    private K8sClient k8sClient;

    /**
     * 1.load k8s config, all the configs come from the server where the k8s on.
     * 2.create configMap
     * 3.create deployment
     * 4.create service
     * 5.create ingress
     *
     * @throws Exception e
     */
    public void deploy() throws Exception {
        //create deployment
        V1Deployment deployment = createDeployment(CONFIG_JSON_VALUE, "deployment-name", "app-1-test", "docker-boot:1.0");

        //create service
        V1Service service = createService(deployment.getMetadata().getName(), "service-name");

        //create ingress
        V1Ingress ingress = createIngress(service.getMetadata().getName(), "ingress-service-deployment");

        log.info("deploy successful! deployment:{}, service:{}, ingress:{}", deployment, service, ingress);
    }

    public V1Deployment createDeployment(String configJsonValue, String deploymentName, String configName, String targetImage) {
        try {
            V1Deployment v1Deployment = (V1Deployment) loadConfigResource("deployment.yml");

            //create config map
            V1ConfigMap v1ConfigMap = createConfigMap(configJsonValue, configName);
            String configMapName = v1ConfigMap.getMetadata().getName();

            //config the deployment
            v1Deployment.getMetadata().setName(deploymentName);
            v1Deployment.getMetadata().getLabels().put(LABEL_KEY, deploymentName);
            v1Deployment.getSpec().getSelector().getMatchLabels().put(LABEL_KEY, deploymentName);
            V1PodTemplateSpec template = v1Deployment.getSpec().getTemplate();
            template.getMetadata().getLabels().put(LABEL_KEY, deploymentName);

            V1Container v1Container = template.getSpec().getContainers().get(0);
            v1Container.setName(deploymentName);

            //your target image
            v1Container.setImage(targetImage);

            //configMap
            V1Volume v1Volume = template.getSpec().getVolumes().get(0);
            V1ConfigMapVolumeSource configMap = v1Volume.getConfigMap();
            //configMap name
            configMap.setName(configMapName);
            V1KeyToPath v1KeyToPath = configMap.getItems().get(0);
            //configMap key
            v1KeyToPath.setKey(CONFIG_JSON);
            //config map's file name
            v1KeyToPath.setPath(CONFIG_JSON);

            //create
            AppsV1Api apiInstance = k8sClient.getAppsV1Api();
            return apiInstance.createNamespacedDeployment(DEFAULT_NAMESPACE, v1Deployment, null, null, null, null);
        } catch (Exception e) {
            log.error("createDeployment error:{}", e.getMessage(), e);
            throw new RuntimeException("createDeployment error!", e);
        }
    }

    public V1Service createService(String deploymentName, String serviceName) {
        try {
            V1Service v1Service = (V1Service) loadConfigResource("service.yml");

            v1Service.getMetadata().setName(serviceName);
            v1Service.getMetadata().getLabels().put(LABEL_KEY, serviceName);
            //deployment name
            v1Service.getSpec().getSelector().put(LABEL_KEY, deploymentName);

            CoreV1Api api = new CoreV1Api();
            //create
            return api.createNamespacedService(DEFAULT_NAMESPACE, v1Service, null, null, null, null);
        } catch (Exception e) {
            log.error("createService error:{}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("createService error!", e);
        }
    }

    private V1ConfigMap createConfigMap(String configJsonValue, String name) {
        try {
            Map<String, String> dataMap = new HashMap<>();
            //String configMapName = CONFIG_MAP_PREFIX + name;

            //key: file name, value: the text of file
            dataMap.put(CONFIG_JSON, configJsonValue);
            V1ConfigMap configMap = (new V1ConfigMap())
                    .apiVersion("v1")
                    .kind("ConfigMap")
                    .metadata(new V1ObjectMeta().name(name).namespace(DEFAULT_NAMESPACE))
                    .data(dataMap);

            CoreV1Api coreV1Api = k8sClient.getCoreV1Api();

            //delete old config map
//            V1Status status = coreV1Api.deleteNamespacedConfigMap(configMapName, DEFAULT_NAMESPACE, null, null,
//                    null, null, null, new V1DeleteOptions());
//            if (status != null && (status.getCode() != 0 || status.getCode() != 200)) {
//                // error
//                log.error("deleteNamespacedConfigMap error");
//                throw new RuntimeException("deleteNamespacedConfigMap error");
//            }

            //create config map
            return coreV1Api.createNamespacedConfigMap(DEFAULT_NAMESPACE, configMap, null, null, null, null);
        } catch (Exception e) {
            log.error("createConfigMap error:{}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("createConfigMap error!", e);
        }
    }

    private V1Ingress createIngress(String serviceName, String ingressName) {
        try {
            V1Ingress v1Ingress = (V1Ingress) loadConfigResource("ingress.yml");

            v1Ingress.getMetadata().setName(ingressName);
            V1IngressRule v1IngressRule = v1Ingress.getSpec().getRules().get(0);
            V1HTTPIngressRuleValue http = v1IngressRule.getHttp();
            V1HTTPIngressPath v1beta1HTTPIngressPath = http.getPaths().get(0);
            v1beta1HTTPIngressPath.setPath("/api/(/|$)(.*)");
            v1beta1HTTPIngressPath.getBackend().setService(new V1IngressServiceBackend().name(serviceName));

            NetworkingV1Api networkingV1Api = new NetworkingV1Api(k8sClient.getApiClient());
            return networkingV1Api.createNamespacedIngress(DEFAULT_NAMESPACE, v1Ingress, null, null, null, null);
        } catch (Exception e) {
            log.error("createIngress {} error:{}", serviceName, e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("createIngress error!", e);
        }
    }

    public Object loadConfigResource(String yml) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(yml);
        InputStream inputStream = classPathResource.getInputStream();
        Reader reader = new InputStreamReader(inputStream);
        Object obj = Yaml.load(reader);
        reader.close();
        return obj;
    }
}
