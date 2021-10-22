package com.example.livecare.bluetoothsdk;

import android.app.Application;
import android.content.Context;

import com.example.livecare.bluetoothsdk.initFunctions.data.local.DbHelper;
import com.example.livecare.bluetoothsdk.initFunctions.data.local.PrefManager;

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PrefManager.initSharedPref(this);
        instance = this;
    }
}
