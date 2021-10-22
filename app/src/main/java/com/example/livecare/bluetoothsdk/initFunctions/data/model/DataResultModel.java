package com.example.livecare.bluetoothsdk.initFunctions.data.model;

import java.util.Map;

public class DataResultModel {
    private long id;
    private Map<String, Object> data;
    private String type;
    private String BTMac;
    private String BTName;
    private Long createdAt;

    public DataResultModel(){

    }

    public DataResultModel(Map<String, Object> data, String deviceType, String mac, String deviceName, long createdAt) {
        this.data = data;
        this.type = deviceType;
        this.BTMac = mac;
        this.BTName = deviceName;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBTMac(String BTMac) {
        this.BTMac = BTMac;
    }

    public void setBTName(String BTName) {
        this.BTName = BTName;
    }

    public void setCreatedAt(Long createdAt) {
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
