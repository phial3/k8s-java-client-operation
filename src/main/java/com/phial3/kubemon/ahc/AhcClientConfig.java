package com.phial3.kubemon.ahc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author WANGJUNJIE2
 */
@Configuration
public class AhcClientConfig {

    @Bean
    public AhcClient ahcClient() {
        return new AhcClient();
    }

}