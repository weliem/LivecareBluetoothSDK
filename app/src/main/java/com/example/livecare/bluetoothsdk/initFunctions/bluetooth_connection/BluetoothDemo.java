package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BluetoothDemo {
    private Context mContext;

    @Inject
    BluetoothDemo(Context context) {
        mContext = context;
    }

    public void displayBle(){
        Toast.makeText(mContext,"Message",Toast.LENGTH_SHORT).show();
        Log.d("TAG", "displayBle: ");
    }
}
