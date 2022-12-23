package org.phial3.k8s.fabric;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1HostPathVolumeSource;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import org.phial3.k8s.deploy.K8sClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FabricDeployment {

    public abstract FabricDeployment deployment();

    @Resource
    private K8sClient k8sClient;

    V1ObjectMeta createDeploymentMetadata(String namespace, String name) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName(name);
        objectMeta.setNamespace(namespace);
        return objectMeta;
    }

    private V1PodTemplateSpec createPodTemplateSpec(String type, String orgName, String peerName,
                                                    List<Container> containers) {
        V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
        v1PodTemplateSpec.setMetadata(new V1ObjectMeta().labels(createSelectorLabels(type, orgName, peerName)));
        v1PodTemplateSpec.setSpec(createPodSpec(containers));
        return v1PodTemplateSpec;
    }

    private Map<String, String> createSelectorLabels(String type, String orgName, String peerName) {
        return new HashMap<String, String>() {
            {
                put("app", "hyperledger");
                put("role", type);
                put("org", orgName);
                put("name", peerName);
            }
        };
    }

    public void deploy() {
        String namespace = "fabrictest";
        ArrayList<Container> containers = new ArrayList<>();
        V1Deployment deployment = createDeployment(namespace, "peer", "peer-org", "peer0", containers);
        try {
            V1Deployment namespacedDeployment =
                    k8sClient.getAppsV1Api().createNamespacedDeployment(namespace, deployment, null, null, null, null);
            System.out.println(namespacedDeployment);
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1Api#createNamespace");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    private V1PodSpec createPodSpec(List<Container> containers) {
        return new V1PodSpec().containers(createContainers(containers)).volumes(createVolumes(new ArrayList<>()));
    }

    public V1Deployment createDeployment(String namespace, String type, String orgName, String peerName,
                                         List<Container> containers) {
        V1DeploymentSpec template =
                new V1DeploymentSpec()
                        .replicas(1)
                        .template(createPodTemplateSpec(type, orgName, peerName, containers))
                        .selector(new V1LabelSelector().matchLabels(createSelectorLabels(type, orgName, peerName)));

        V1Deployment deployment = new V1Deployment();

        deployment.setApiVersion("apps/v1");
        deployment.setKind("Deployment");
        deployment.setSpec(template);
        deployment.setMetadata(createDeploymentMetadata(namespace, String.format("%s-%s", type, peerName)));
        return deployment;
    }

    protected static List<V1Volume> createVolumes(List<LocalVolume> localVolumes) {
        List<V1Volume> volumes = new ArrayList<>();

        for (LocalVolume localVolume : localVolumes) {
            volumes.add(new V1Volume().name(localVolume.getName())
                    .hostPath(new V1HostPathVolumeSource().path(localVolume.getPath()).type(localVolume.getType())));
        }
        volumes.add(new V1Volume().name("current-dir")
                .hostPath(new V1HostPathVolumeSource().path("/workspace").type("Directory")));
        volumes
                .add(new V1Volume().name("run").hostPath(new V1HostPathVolumeSource().path("/var/run").type("Directory")));
        return volumes;
    }

    public List<V1Container> createContainers(List<Container> containers) {
        List<V1Container> v1Containers = new ArrayList<>();
        for (Container container : containers) {
            v1Containers.add(createContainer(container));
        }
        return v1Containers;
    }

    private V1Container createContainer(Container container) {
        V1Container v1Container = new V1Container();
        v1Container.setName(container.getName());
        v1Container.setImage(container.getImage());
        v1Container.setEnv(createEnvVars(createEnvVarMap()));

        v1Container.setVolumeMounts(createVolumeMounts(container.getVolumeMounts()));
        v1Container.setWorkingDir(container.getWorkingDir());
        v1Container.setCommand(container.getCommands());
        v1Container.setPorts(creatContainerPorts(container.getPorts()));
        return v1Container;
    }

    private List<V1ContainerPort> creatContainerPorts(List<Integer> ports) {
        List<V1ContainerPort> retList = new ArrayList<>();
        for (Integer port : ports) {
            retList.add(new V1ContainerPort().containerPort(port));
        }
        return retList;
    }

    protected abstract Map<String, String> createEnvVarMap();

    private static V1EnvVar createEnvVar(String name, String value) {
        V1EnvVar v1EnvVar = new V1EnvVar();
        v1EnvVar.setName(name);
        v1EnvVar.setValue(value);
        return v1EnvVar;
    }

    protected static List<V1EnvVar> createEnvVars(Map<String, String> envVarsMap) {
        List<V1EnvVar> envVars = new ArrayList<>();
        for (Map.Entry<String, String> entry : envVarsMap.entrySet()) {
            envVars.add(createEnvVar(entry.getKey(), entry.getValue()));
        }
        return envVars;
    }

    protected abstract List<V1VolumeMount> createVolumeMounts(List<Map<String, String>> paths);

    public static V1VolumeMount createVolumeMount(String name, String path) {
        V1VolumeMount v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName(name);
        v1VolumeMount.setMountPath(path);
        return v1VolumeMount;
    }

    public static V1VolumeMount createVolumeMount(String name, String path, String subPath) {
        V1VolumeMount v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName(name);
        v1VolumeMount.setMountPath(path);
        v1VolumeMount.setSubPath(subPath);
        return v1VolumeMount;
    }
}
