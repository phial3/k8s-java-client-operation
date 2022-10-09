package com.phial3.k8s.bean;

import lombok.Data;

@Data
public class AppInfo {
    String name;            // metadata-name, selector-app, labels-app, containers-name
    String image;           // template-spec-image
    int containerPort;      //
    int initInstanceNum;    // spec-replicas
    int servicePort;        // service-spec-ports-nodePort
}
