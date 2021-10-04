package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class BPAndesFit {
    private final String TAG = "BPAndesFit:";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private Handler handler;
    private Runnable runnable;
    private int flag = 0;
    private int lastDataRegistered = 0;
    private boolean closeBP = true;
    private final int delay = 5000;

    public BPAndesFit(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        startNotify(characteristic);
                    }
                }
            }
        }
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        Log.d(TAG, "onNotifySuccess: ");
                        checkIfDeviceIsOn();
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        Log.d(TAG, "onNotifyFailure: ");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }


    private void calculateResults(String value) {
        if (value.length() == 36 && value.substring(0, 2).equalsIgnoreCase("1e")) {
            closeBP = false;
            if(handler != null && runnable != null){
                handler.removeCallbacks(runnable);
            }

            String sys = new BigInteger(value.substring(4, 6), 16).toString();
            String dia = new BigInteger(value.substring(8, 10), 16).toString();
            String pul = new BigInteger(value.substring(16, 18), 16).toString();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("sys", sys);
            dataMap.put("dia", dia);
            dataMap.put("pulse", pul);
            dataMap.put("ahr", "");

            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue);
            BleManager.getInstance().disconnect(bleDevice);
        } else {
            flag++;
        }
    }

    private void checkIfDeviceIsOn() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (lastDataRegistered != 0) {
                    if (lastDataRegistered == flag) {
                        if (closeBP) {
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (bleDevice != null) {
                                    BleManager.getInstance().disconnect(bleDevice);
                                }
                            }, 1000);
                        }
                        handler.removeCallbacks(runnable);
                    } else {
                        handler.postDelayed(this, delay);
                    }
                } else {
                    handler.postDelayed(this, delay);
                }
                lastDataRegistered = flag;
            }
        };
        handler.postDelayed(runnable, delay);
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
