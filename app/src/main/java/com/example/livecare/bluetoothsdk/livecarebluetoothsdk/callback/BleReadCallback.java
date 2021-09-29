package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback;


import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;

public abstract class BleReadCallback extends BleBaseCallback {

    public abstract void onReadSuccess(byte[] data);

    public abstract void onReadFailure(BleException exception);

}
