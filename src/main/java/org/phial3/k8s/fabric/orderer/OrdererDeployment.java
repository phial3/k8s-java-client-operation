/**
 * Project Name:mallivora-fabric-k8s File Name:OrdererDeployment.java Package Name:com.mallivora.fabric.k8s.orderer
 * Date:2020年3月16日上午11:11:40 Copyright (c) 2020, wangchao9@asiainfo.com All Rights Reserved.
 */

package org.phial3.k8s.fabric.orderer;

import io.kubernetes.client.openapi.models.V1VolumeMount;
import org.phial3.k8s.fabric.FabricDeployment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.phial3.k8s.fabric.FabricContainerEnvConstant.*;


/**
 * ClassName:OrdererDeployment <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2020年3月16日 上午11:11:40 <br/>
 *
 * @author 9527
 * @see
 * @since JDK 1.8
 */
@Component
public class OrdererDeployment extends FabricDeployment {


    @Override
    public FabricDeployment deployment() {
        return this;
    }

    @Override
    protected Map<String, String> createEnvVarMap() {
        Map<String, String> evnVarMap = new HashMap<>();
        evnVarMap.put(FABRIC_LOGGING_SPEC_NAME, FABRIC_LOGGING_SPEC);
        evnVarMap.put(ORDERER_GENERAL_LISTENADDRESS_NAME, ORDERER_GENERAL_LISTENADDRESS);
        evnVarMap.put(ORDERER_GENERAL_GENESISMETHOD_NAME, ORDERER_GENERAL_GENESISMETHOD);
        evnVarMap.put(ORDERER_GENERAL_GENESISFILE_NAME, ORDERER_GENERAL_GENESISFILE);
        evnVarMap.put(ORDERER_GENERAL_LOCALMSPID_NAME, ORDERER_GENERAL_LOCALMSPID);
        evnVarMap.put(ORDERER_GENERAL_LOCALMSPDIR_NAME, ORDERER_GENERAL_LOCALMSPDIR);
        evnVarMap.put(ORDERER_GENERAL_TLS_ENABLED_NAME, ORDERER_GENERAL_TLS_ENABLED);
        evnVarMap.put(ORDERER_GENERAL_TLS_PRIVATEKEY_NAME, ORDERER_GENERAL_TLS_PRIVATEKEY);
        evnVarMap.put(ORDERER_GENERAL_TLS_CERTIFICATE_NAME, ORDERER_GENERAL_TLS_CERTIFICATE);
        evnVarMap.put(ORDERER_GENERAL_TLS_ROOTCAS_NAME, ORDERER_GENERAL_TLS_ROOTCAS);
        evnVarMap.put(ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE_NAME, ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE);
        evnVarMap.put(ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY_NAME, ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY);
        evnVarMap.put(ORDERER_GENERAL_CLUSTER_ROOTCAS_NAME, ORDERER_GENERAL_CLUSTER_ROOTCAS);
        return evnVarMap;
    }

    protected List<V1VolumeMount> createVolumeMounts(List<Map<String, String>> paths) {
        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
        v1VolumeMounts.add(createVolumeMount("current-dir", "/var/hyperledger/orderer/msp",
                "crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/msp"));
        v1VolumeMounts.add(createVolumeMount("current-dir", "/var/hyperledger/orderer/tls",
                "crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls"));
        v1VolumeMounts.add(createVolumeMount("current-dir", "/var/hyperledger/orderer/orderer.genesis.block",
                "crypto-config/genesis.block"));
        v1VolumeMounts.add(createVolumeMount("run", "/host/var/run"));
        return v1VolumeMounts;
    }

}
