package com.example.livecare.bluetoothsdk.initFunctions.data.model;

import java.util.Map;

public class DataResultModel {
    private Map<String, Object> data;
    private String type;
    private String BTMac;
    private String BTName;
    private Long createdAt;

    public DataResultModel(Map<String, Object> data, String deviceType, String mac, String deviceName, long createdAt) {
        this.data = data;
        this.type = deviceType;
        this.BTMac = mac;
        this.BTName = deviceName;
        this.createdAt = createdAt;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    public String getBTMac() {
        return BTMac;
    }

    public String getBTName() {
        return BTName;
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}
