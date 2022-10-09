package com.phial3.k8s.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableApolloConfig
public class AppConfig extends ApolloEnvConfig {

    public AppConfig() {
        super();
    }

    public String getKubConfigYaml() {
        return baasConfig.get(source).getProperty("kubconfig", "");
    }

    public List<String> getIpList() {
        String ipList = baasConfig.get(source).getProperty("ipPool", "");
        return Arrays.asList(ipList.split(","));
    }

    public String getImagesName(String nodeType) {
        return baasConfig.get(source).getProperty("image-" + nodeType, "");
    }

    public String getK8sNamespace() {
        return baasConfig.get(source).getProperty("k8sNamespace", "");
    }

    public String getMysqlUser() {
        return baasConfig.get(source).getProperty("mysqlUser", "");
    }

    public String getMysqlDB() {
        return baasConfig.get(source).getProperty("mysqlDB", "");
    }

    public Integer getK8sStartPort() {
        return baasConfig.get(source).getIntProperty("k8sStartPort", 0);
    }

    public Integer getK8sEndPort() {
        return baasConfig.get(source).getIntProperty("k8sEndPort", 0);
    }


    public String getDeployEnv() {
        return baasConfig.get(source).getProperty("deployEnv", "");
    }

    public boolean kafkaTestSwitch() {
        return appConfig.getBooleanProperty("kafkaTestSwitch", false);
    }

    public List<String> getPVCList() {
        String pvcList = baasConfig.get(source).getProperty("pvcPool", "");
        return Arrays.stream(pvcList.split(",")).filter(o -> !StringUtils.isBlank(o)).collect(Collectors.toList());
    }

    public String[] getKafkaTestChainNames() {
        String chainNames = appConfig.getProperty("kafkaTestChainNames", "");
        if (StringUtils.isBlank(chainNames)) {
            return new String[0];
        }
        return chainNames.split(",");
    }

    public String getThreadPoolConfig() {
        return appConfig.getProperty("threadPoolConfig", "");
    }


    public String getDBType() {
        return baasConfig.get(source).getProperty("dbType", "");
    }

    public boolean getDisableBlockFileDb() {
        return baasConfig.get(source).getBooleanProperty("disableBlockFileDb", false);
    }

    public Config getAppConfig() {
        return appConfig;
    }

    public Config getBaasConfig(String source) {
        return baasConfig.get(source);
    }


    public String getQueryContractMethods() {
        return baasConfig.get(source).getProperty("queryContract", "");
    }

    public String getInvokeContractMethods() {
        return baasConfig.get(source).getProperty("invokeContract", "");
    }

    public String getChainmakerGoResource() {
        String resources = baasConfig.get(source).getProperty("k8sResource", "");
        JSONObject resourceInfos = JSON.parseObject(resources);
        return resourceInfos.getString("chainmaker-go");
    }

    public String getChainmakerCAResource() {
        String resources = baasConfig.get(source).getProperty("k8sResource", "");
        JSONObject resourceInfos = JSON.parseObject(resources);
        return resourceInfos.getString("chainmaker-ca");
    }

    public String getContractDockerVmResource() {
        String resources = baasConfig.get(source).getProperty("k8sResource", "");
        JSONObject resourceInfos = JSON.parseObject(resources);
        return resourceInfos.getString("contract-docker-vm");
    }

    public String getDataBase(String baas) {
        return baasConfig.get(baas).getProperty("database", "");
    }

    public String getExposeHeaders() {
        return appConfig.getProperty("Access-Control-Expose-Headers", "get, post, delete, common, put, patch, head, Content-type");
    }

    public String getAllowHeaders() {
        return appConfig.getProperty("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, sessionid, app, attr, rcuuid, rsid, cache-control, userName, source, X-Litemall-Admin-Token, rcver, selectedChannel,selectedChannel,weexid, unionid,mark,Authorization, source, auth");
    }

    public String getAllowMethods() {
        return appConfig.getProperty("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
    }

    public String getRedisClient() {
        return baasConfig.get(source).getProperty("redis", "");
    }
}
