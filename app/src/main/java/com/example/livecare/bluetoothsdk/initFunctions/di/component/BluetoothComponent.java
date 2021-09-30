package com.example.livecare.bluetoothsdk.initFunctions.di.component;

import android.app.Application;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDemo;
import com.example.livecare.bluetoothsdk.initFunctions.di.module.BluetoothModule;
import javax.inject.Singleton;
import dagger.Component;

@Singleton
@Component(modules = BluetoothModule.class)
public interface BluetoothComponent {
    void inject(Application liveCareMainClass);

    BluetoothDemo getBleDemo();
}
