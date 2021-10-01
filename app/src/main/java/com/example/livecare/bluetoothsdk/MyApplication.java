package com.example.livecare.bluetoothsdk;

import android.app.Application;
import android.content.Context;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.ApplicationComponent;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.DaggerApplicationComponent;
import com.example.livecare.bluetoothsdk.initFunctions.di.module.ApplicationModule;

import javax.inject.Inject;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Inject
    BluetoothConnection bluetoothConnection;

    public static MyApplication getInstance() {
        return instance;
    }

    private ApplicationComponent applicationComponent;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        getNetComponent().inject(this);
        bluetoothConnection.setMessage();
    }

    public ApplicationComponent getNetComponent() {
        return applicationComponent;
    }
}
