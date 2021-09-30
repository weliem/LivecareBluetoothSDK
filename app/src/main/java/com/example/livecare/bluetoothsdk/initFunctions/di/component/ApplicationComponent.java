package com.example.livecare.bluetoothsdk.initFunctions.di.component;

import com.example.livecare.bluetoothsdk.MyApplication;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.di.module.ApplicationModule;
import javax.inject.Singleton;
import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MyApplication demoApplication);

    BluetoothConnection getBluetoothConnection();
}

