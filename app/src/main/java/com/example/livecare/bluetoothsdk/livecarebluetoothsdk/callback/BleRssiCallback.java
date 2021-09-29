package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback;


import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;

public abstract class BleRssiCallback extends BleBaseCallback{

    public abstract void onRssiFailure(BleException exception);

    public abstract void onRssiSuccess(int rssi);

}