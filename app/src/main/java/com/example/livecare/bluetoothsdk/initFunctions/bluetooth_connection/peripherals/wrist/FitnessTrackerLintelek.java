package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.wrist;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.service.TeleHealthScanBackgroundPresenter;
import com.example.livecare.bluetoothsdk.initFunctions.service.TeleHealthService;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Constants;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleGattCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FitnessTrackerLintelek {

    private BleDevice bleDevice;
    private final TeleHealthScanBackgroundPresenter teleHealthScanBackgroundPresenter;

    private BluetoothGattCharacteristic characteristicWrite;
    private Handler mHandler;

    public FitnessTrackerLintelek(TeleHealthService mContext, TeleHealthScanBackgroundPresenter teleHealthScanBackgroundPresenter) {
        this.teleHealthScanBackgroundPresenter = teleHealthScanBackgroundPresenter;
    }

    public void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {}

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                teleHealthScanBackgroundPresenter.resumeScan();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                if(gatt != null){
                    onConnectedSuccess(bleDevice,gatt);
                }else {
                    BleManager.getInstance().disconnect(bleDevice);
                }

                teleHealthScanBackgroundPresenter.watchConnected();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status, int disconnectFlag) {
                teleHealthScanBackgroundPresenter.watchDisConnected();
                Constants.currentTimeForLastTelehealthServiceFitnessTracker = Calendar.getInstance().getTime().getTime();
                if(mHandler!=null){
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        });
    }


    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00000af7")) {
                    startNotify(characteristic);
                }
                if (characteristic.getUuid().toString().contains("00000af6")) {
                    characteristicWrite = characteristic;
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
                        startWriteCommand(characteristicWrite,"02a0");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String formatHexString = HexUtil.formatHexString(data);
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(() -> startWriteCommand(characteristicWrite,"02a0"), 10000);

                        if(formatHexString.startsWith("02a0")){
                            calculateResult(formatHexString);
                        }
                    }
                });
    }

    private void calculateResult(String formatHexString) {
        String steps = new BigInteger(formatHexString.substring(6, 8) + formatHexString.substring(4, 6), 16).toString();
        String kCal = new BigInteger(formatHexString.substring(14, 16) + formatHexString.substring(12, 14), 16).toString();
        String hr = new BigInteger(formatHexString.substring(34, 38), 16).toString();

        if(!hr.equals("0")){
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("steps", steps);
            objectMap.put("kCal", kCal);
            objectMap.put("hr", hr);
            //bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Fitness.stringValue);
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic , String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
}
