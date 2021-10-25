package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection;

import java.util.Map;

public interface BluetoothDataResult {
    void authenticationStatus(String s);

    void onScanningStatus(String onScan);

    void onStartConnect(String deviceName);
    
    void OnConnectedSuccess(String deviceName);

    void OnConnectFail(String deviceName, String message);

    void onDisConnected(String deviceName);

    void onDataReceived(Map<String, Object> data, String deviceName);
}
