package com.phial3.k8s.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApolloEnvConfig {

    public static final String source = "Umetrip";

    public static final String apolloName = "BaaS.Chainmaker";

    public static final String baasListName = "baas";

    protected Config appConfig = ConfigService.getConfig(apolloName);

    protected Map<String, Config> baasConfig = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        initBaasConfig();
    }

    public boolean containsBaas(String baas) {
        return baasConfig.containsKey(baas);
    }

    public Config getEnvConfig(String baas) {
        return baasConfig.get(baas);
    }

    public String[] getBaasList() {
        return appConfig.getProperty(baasListName, "").split(",");
    }

    public void initBaasConfig() {
        Map<String, Config> newBaasConfig = new ConcurrentHashMap<>();
        for (String baas : getBaasList()) {
            newBaasConfig.put(baas, ConfigService.getConfig(apolloName + "." + baas));
        }
        baasConfig = newBaasConfig;
    }

    public boolean enableEvent() {
        return appConfig.getBooleanProperty("enableEvent", false);
    }
}
