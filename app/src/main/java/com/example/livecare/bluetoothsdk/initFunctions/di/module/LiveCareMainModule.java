package com.example.livecare.bluetoothsdk.initFunctions.di.module;

import android.content.Context;

import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;

import dagger.Module;
import dagger.Provides;

@Module
public class LiveCareMainModule {
    private LiveCareMainClass liveCareMainClass;

    public LiveCareMainModule(LiveCareMainClass liveCareMain) {
        liveCareMainClass = liveCareMain;
    }

    @Provides
    LiveCareMainClass provideContext() {
        return liveCareMainClass;
    }

}
