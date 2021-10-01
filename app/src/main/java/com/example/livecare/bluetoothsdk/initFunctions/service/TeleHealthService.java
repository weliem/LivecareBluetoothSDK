package com.example.livecare.bluetoothsdk.initFunctions.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.List;

public class TeleHealthService extends Service {

    private static final String TAG = "TeleHealthService";
    private TeleHealthScanBackgroundPresenter teleHealthScanBackgroundPresenter;
    private int resetCount = 0;
    private BleDevice bleDevice;
    private int flag;
    private PowerManager.WakeLock wl;
    Handler handlerToStartBluetoothActivity = new Handler();
    Runnable runnableToStartBluetoothActivity;
    //private IHealthConnection iHealthConnection;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: TeleHealthService");
        teleHealthScanBackgroundPresenter = new TeleHealthScanBackgroundPresenter(this);
        startForegroundService();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "telehealthLockTag:");
        wl.acquire(1000 * 60 * 60 * 24 * 10);//10 days
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       /* if (intent != null) {
             if (intent.getAction() != null) {
               if(intent.getAction().equals(CONNECT_IHEALTH_DEVICE)){
                    if(iHealthConnection!=null){
                        iHealthConnection.startScanning();
                    }
                }
            }
        }*/
        return START_NOT_STICKY;
    }

    private void onCreateMethod() {
        flag = 0;
        BleManager.getInstance().init(getApplication());
        if (BleManager.getInstance().getBluetoothAdapter() == null) {
            BleManager.getInstance().setBluetoothAdapter();
        }
        if (!BleManager.getInstance().isBlueEnable()) {
            BleManager.getInstance().enableBluetooth();
            isBlueEnable();
        } else {
            BleManager.getInstance()
                    .enableLog(true)
                    .setReConnectCount(1, 500)
                    .setConnectOverTime(20000)
                    .setOperateTimeout(100000);
            BleManager.getInstance().isScanning(true);
            if(teleHealthScanBackgroundPresenter!=null){
                teleHealthScanBackgroundPresenter.iniFunction();
            }
        }
    }

    private void isBlueEnable() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: isBlueEnable "+BleManager.getInstance().isBlueEnable());
                if (BleManager.getInstance().isBlueEnable()) {
                    BleManager.getInstance()
                            .enableLog(true)
                            .setReConnectCount(1, 500)
                            .setConnectOverTime(20000)
                            .setOperateTimeout(100000);
                    BleManager.getInstance().isScanning(true);
                    if(teleHealthScanBackgroundPresenter!=null){
                        teleHealthScanBackgroundPresenter.iniFunction();
                        handler.removeCallbacks(this);
                    }
                } else {
                    if (BleManager.getInstance().getBluetoothAdapter() == null) {
                        Log.d(TAG, "run: isBlueEnable null ");
                        handler.removeCallbacks(this);
                        onCreateMethod();
                    } else {
                        if (flag == 10) {
                            onCreateMethod();
                            handler.removeCallbacks(this);
                        }
                        flag++;
                        Log.d(TAG, "run: isBlueEnable null post delay");
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        }, 1000);
    }

    private void onStopMethod() {
        BleManager.getInstance().isScanning(false);
        BleManager.getInstance().cancelScan();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        BleManager.getInstance().clearCharacterCallback(bleDevice);
        BleManager.getInstance().disableBluetooth();
        /*if(iHealthConnection != null){
            iHealthConnection.onDestroy();
        }*/
    }

    public void setDeviceFound(String deviceName, BleDevice bleDevice, String devicesOrigin) {
        this.bleDevice = bleDevice;
        sendDataToTeleHealthReceiver(deviceName,bleDevice,devicesOrigin);
      /*  if (devicesOrigin.equals("1")){
            if(bleDevice.getName()!=null){
                if(iHealthConnection == null) {
                    iHealthConnection = new IHealthConnection(this);
                }
                if(BLE_PULSE_OXIMETER_IHEALTH.equals(bleDevice.getName())){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_PO3, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_BG5S)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_BG5S, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_HS2S)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_HS2S, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_550BT)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_550BT, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_PT3SBT)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_PT3SBT, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_TS28B)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_TS28B, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_ECG3)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_ECG3, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_HS4S)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_HS4S, bleDevice,isBackground);
                }else if(bleDevice.getName().contains(iHealthDevicesManager.TYPE_HS2)){
                    iHealthConnection.initFunction(deviceName,iHealthDevicesManager.TYPE_HS2, bleDevice,isBackground);
                }
            }
        }else {

        }*/
    }

   /* public void checkIfBackGroundIHealthIsConnected() {
        if(iHealthConnection != null) {
            iHealthConnection.checkIfBackGroundIHealthIsConnected();
        }
    }*/

    public void sendDataToTeleHealthReceiver(String deviceName, BleDevice bleDevice, String devicesOrigin){
        int delay;
        if (Utils.isPlugged(this)) {
            delay = 500;
        } else {
            delay = 2000;
        }
        runnableToStartBluetoothActivity = () -> {
            Intent local = new Intent();
            local.setAction("update.ui.with.device");
            local.putExtra("deviceName", deviceName);
            local.putExtra("bluetoothDevice", bleDevice);
            local.putExtra("devicesOrigin", devicesOrigin);
            sendBroadcast(local);
        };
        handlerToStartBluetoothActivity.postDelayed(runnableToStartBluetoothActivity, delay);
    }

    public void broadcastBluetoothFragment(String action , String value) {
        Intent local = new Intent();
        local.setAction("broadcast.bluetoothActivity");
        local.putExtra("action",action);
        local.putExtra("value",value);
        sendBroadcast(local);
    }

    private void startForegroundService() {
        onCreateMethod();
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else {
            createNotificationChannel();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LiveCareMainClass.class), 0);//todo check class
            Notification notification = new NotificationCompat.Builder(this, "1")
                    .setContentTitle("Foreground Service")
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleappsim123";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void createNotificationChannel() {
/*        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = null;
        pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("title")
                .setContentText("description")
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))
                .setSmallIcon(R.drawable.cellular_icon)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("title").bigText("description"));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());*/
    }

    public void resetBluetooth() {
        resetCount++;
        if (resetCount == 1) {
            Utils.resetTeleHealthService();
        }
    }

    @Override
    public void onDestroy() {
        onStopMethod();
        teleHealthScanBackgroundPresenter.onDestroy();
        wl.release();
        teleHealthScanBackgroundPresenter = null;
        if (handlerToStartBluetoothActivity != null){
            handlerToStartBluetoothActivity.removeCallbacks(runnableToStartBluetoothActivity);
        }
        super.onDestroy();
    }

    public boolean checkIfAppIsRunning() {
        Log.d(TAG, "checkIfAppIsRunning: ");
        return !(!isAppOnForeground(this) && checkIfScreenIsOn());
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                Log.d(TAG, "isAppOnForeground: ");
                return true;
            }
        }
        return false;
    }

    private boolean checkIfScreenIsOn() {
        PowerManager pm = (PowerManager) this.getSystemService((POWER_SERVICE));
        if (null != pm) {
            return pm.isInteractive();
        } else {
            return false;
        }
    }
}

