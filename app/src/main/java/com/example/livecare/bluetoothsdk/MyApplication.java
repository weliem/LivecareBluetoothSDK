package com.example.livecare.bluetoothsdk;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;
    private String TAG = "MyApplication";

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
