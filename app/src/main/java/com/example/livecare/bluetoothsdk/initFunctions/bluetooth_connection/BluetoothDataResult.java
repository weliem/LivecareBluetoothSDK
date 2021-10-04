package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection;

import java.util.Map;

public interface BluetoothDataResult {
    void onStartConnect(String deviceName);
    
    void OnConnectedSuccess(String deviceName);

    void OnConnectFail(String message);

    void onDisConnected(String deviceName);

    void onDataReceived(Map<String, Object> data, String deviceName);
}
