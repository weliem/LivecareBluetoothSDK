package com.example.livecare.bluetoothsdk.initFunctions;

import java.util.ArrayList;

public class IHealthConnectedDevices {

    private static IHealthConnectedDevices instance;
    private ArrayList<String> iHealthBleList = new ArrayList<>();
    private ArrayList<String> backgroundBleList = new ArrayList<>();
    private boolean isConnecting = false;

    public static IHealthConnectedDevices getInstance() {
        if (instance == null) {
            instance = new IHealthConnectedDevices();
        }
        return instance;
    }

    void addToList(String deviceName){
        iHealthBleList.add(deviceName);
    }

    void removeFromList(String deviceName){
        iHealthBleList.remove(deviceName);
    }

    ArrayList<String> getAllList(){
        return iHealthBleList;
    }

    void clearList(){
        iHealthBleList.clear();
    }

    void setIsConnecting(boolean isConnecting){
        this.isConnecting = isConnecting;
    }

    public boolean getIsConnecting(){
        return isConnecting;
    }

    void addToBackgroundBleMacList(String mac) {
        backgroundBleList.add(mac);
    }

    void removeBackgroundBleMacList(String mac) {
        backgroundBleList.remove(mac);
    }

    void clearBackgroundBleMacList() {
        backgroundBleList.clear();
    }

    public ArrayList<String> getAllBackgroundBleMacList() {
        return backgroundBleList;
    }
}
