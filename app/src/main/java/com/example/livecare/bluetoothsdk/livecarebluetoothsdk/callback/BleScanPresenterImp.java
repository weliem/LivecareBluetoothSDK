package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback;


import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;

public interface BleScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanning(BleDevice bleDevice);

}
