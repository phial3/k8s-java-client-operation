package com.phial3.k8s.bean;

import lombok.Data;

@Data
public class InstanceInfo {
    String name;
    String createTime;
    String innerIp;
    String uid;
}
