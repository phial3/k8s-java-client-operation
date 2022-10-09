package com.phial3.k8s;


import com.phial3.k8s.deploy.K8sClient;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest(classes = {K8sClientApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class K8sClientTest {

    @Resource
    private K8sClient k8sClient;

    @Test
    public void testPodList() {
        V1PodList podList = k8sClient.getAllPodList();
        System.out.println(podList);

    }
}
