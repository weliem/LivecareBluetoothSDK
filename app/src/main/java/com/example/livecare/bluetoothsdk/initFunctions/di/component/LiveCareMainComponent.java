package com.example.livecare.bluetoothsdk.initFunctions.di.component;

import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;
import com.example.livecare.bluetoothsdk.initFunctions.di.PerLiveCare;
import com.example.livecare.bluetoothsdk.initFunctions.di.module.LiveCareMainModule;

import dagger.Component;

@PerLiveCare
@Component(dependencies = ApplicationComponent.class, modules = LiveCareMainModule.class)
public interface LiveCareMainComponent {
    void inject(LiveCareMainClass liveCareMainClass);
}
