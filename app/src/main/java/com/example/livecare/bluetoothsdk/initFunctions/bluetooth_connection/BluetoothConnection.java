package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection;

import android.content.Context;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BluetoothConnection {
    private Context mContext;

    @Inject
    public BluetoothConnection(Context context) {
        mContext = context;
    }

    public void setMessage(){
        Toast.makeText(mContext,"Message",Toast.LENGTH_SHORT).show();
    }
}
