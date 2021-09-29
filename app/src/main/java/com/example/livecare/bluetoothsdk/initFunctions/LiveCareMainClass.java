package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;
    private int callBackId;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }

    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    public void init(Application app) {
        application = app;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();
        //CallIHealthConnection();
        //iHealthDevicesManager.getInstance().startDiscovery(getDiscoveryTypeEnum("BP550BT"));
    }

   /* private DiscoveryTypeEnum getDiscoveryTypeEnum(String deviceName) {
        try {
            for (DiscoveryTypeEnum type : DiscoveryTypeEnum.values()) {
                if (deviceName.equals(type.name())) {
                    return type;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getDiscoveryTypeEnum: %s"+ e.getMessage());
        }
        return null;
    }

    public void CallIHealthConnection() {
        iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {
            @Override
            public void onScanDevice(String mac, String deviceType, int rssi, Map manufactureData) {
                Log.d(TAG, "miHealthDevicesCallback onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " - manufactorData:" + manufactureData);
            }

            @Override
            public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactureData) {
                Log.d(TAG, "miHealthDevicesCallback onDeviceConnectionStateChange: " + "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorId:" + errorID + " -manufactureData:" + manufactureData);
            }

            @Override
            public void onScanError(String reason, long latency) {
                Log.d(TAG, "miHealthDevicesCallback onScanError: " + reason + " please wait for: " + latency + " ms");
            }

            @Override
            public void onScanFinish() {
                Log.d(TAG, "miHealthDevicesCallback onScanFinish: ");

                super.onScanFinish();
            }
        };

        callBackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
    }*/

        private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: "+intent.getExtras().getParcelable("bluetoothDevice"));
                Log.d(TAG, "onReceive: "+intent.getStringExtra("devicesOrigin"));
                Log.d(TAG, "onReceive: "+intent.getStringExtra("deviceName"));
            }
        };

   public void destroy(){
       application.unregisterReceiver(bluetoothDeviceReceiver);
       //iHealthDevicesManager.getInstance().unRegisterClientCallback(callBackId);
   }
}
