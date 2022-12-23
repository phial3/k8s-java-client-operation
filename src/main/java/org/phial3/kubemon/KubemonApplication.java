package org.phial3.kubemon;

import org.phial3.kubemon.metrics.KubernetesMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class KubemonApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder(KubemonApplication.class).build();
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Resource
    private KubernetesMetricsService nodeMetricsService;
    @Value("${schedule-task-enabled:false}")
    private boolean scheduleTaskEnabled;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
        };
    }

    @Scheduled(initialDelay = 5000, fixedDelayString = "${schedule-interval-milliseconds:60000}")
    public void schedule() {
        if (scheduleTaskEnabled) {
        }
    }
}
