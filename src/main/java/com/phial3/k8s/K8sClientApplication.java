package com.phial3.k8s;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableApolloConfig
@SpringBootApplication
public class K8sClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(K8sClientApplication.class, args);
    }

}
