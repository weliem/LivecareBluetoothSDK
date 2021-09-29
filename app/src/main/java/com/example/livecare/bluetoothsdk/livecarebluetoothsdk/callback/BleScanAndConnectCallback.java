package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback;


import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;

public abstract class BleScanAndConnectCallback extends BleGattCallback implements BleScanPresenterImp {

    public abstract void onScanFinished(BleDevice scanResult);

    public void onLeScan(BleDevice bleDevice) {
    }

}
