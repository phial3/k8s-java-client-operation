package com.phial3.k8s;


import com.phial3.k8s.deploy.K8sClient;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@SpringBootTest(classes = {K8sClientApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class K8sClientTest {

    @Resource
    private K8sClient k8sClient;

    @Test
    public void testPodList() {
        List<V1Pod> podList = k8sClient.getPodList();
        for (V1Pod pod : podList) {
            System.out.println(pod);
        }

    }
}
