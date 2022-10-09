package com.phial3.k8s.bean;

import lombok.Data;

import java.util.List;

@Data
public class ServiceInfo {
    AppInfo appInfo;
    String startTime;
    String UID;
    String status;
    int instanceNum;
    List<InstanceInfo> instances;
}
