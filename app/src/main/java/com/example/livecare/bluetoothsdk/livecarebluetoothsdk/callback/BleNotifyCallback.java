package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback;


import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;

public abstract class BleNotifyCallback extends BleBaseCallback {

    public abstract void onNotifySuccess();

    public abstract void onNotifyFailure(BleException exception);

    public abstract void onCharacteristicChanged(byte[] data);

}
