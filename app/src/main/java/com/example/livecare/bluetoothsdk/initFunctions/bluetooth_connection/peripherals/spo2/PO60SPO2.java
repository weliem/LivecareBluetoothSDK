package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PO60SPO2 {
    private final String TAG = "PO60SPO2";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic gattCharacteristicWrite;
    private BluetoothGattCharacteristic gattCharacteristicNotify;
    private String mergedResult = "";
    private Handler handler;
    private Runnable runnable;
    private final int delay = 500;
    private int lastDataRegistered = 0;
    private int flag = 0;

    public PO60SPO2(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("0000ff01")) {
                    gattCharacteristicWrite = characteristic;
                }
                if (characteristic.getUuid().toString().contains("0000ff02")) {
                    gattCharacteristicNotify = characteristic;
                }
            }
        }
        if (Utils.checkPairedDevices(bleDevice.getMac())) {
            startNotify(gattCharacteristicNotify);
        } else {
            if (Utils.isNotOtherBleDeviceConnected()) {
                Log.d(TAG, "onConnectedSuccess: enter pin");
                Utils.pair(bleDevice.getDevice());
                //bluetoothConnection.setPin(bleDevice.getDevice(), 2);
            } else {
                Utils.teleHealthScanBroadcastReceiver(true);
                BleManager.getInstance().disconnect(bleDevice);
            }
        }

    }

    public void startNotifyCustom() {
        //isPaired = true;
        startNotify(gattCharacteristicNotify);
    }


    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        Utils.teleHealthScanBroadcastReceiver(true);
                        startWriteCommand(gattCharacteristicWrite, checkSum("9900"));//997F19
                        checkIfAllDataWasReceived();
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        flag++;
                        mergedResult += HexUtil.formatHexString(data);
                    }
                });
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic, String command) {
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

    private String checkSum(String checksum) {
        int sum = 0;
        char[] value = checksum.toCharArray();

        try {
            for (int i = 0; i < checksum.length(); i += 2) {
                String a = new StringBuilder().append("").append(value[i]).append(value[i + 1]).toString();
                sum += Integer.parseInt(a, 16);
            }
        } catch (Exception ignored) {}

        int summod = (sum % 256);
        return checksum + Integer.toHexString(summod & 0x7F);
    }

    private void checkIfAllDataWasReceived() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (lastDataRegistered != 0) {
                    if (lastDataRegistered == flag) {
                        handler.removeCallbacks(runnable);
                        calculateResult();
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

    private void calculateResult() {
        List<String> resultParts = splitEqually(mergedResult);
        String lastResult = "";
        int size = 0;
        for (String s : resultParts) {
            size++;
            lastResult = s;
        }
        if (size == 10) {
            startWriteCommand(gattCharacteristicWrite, checkSum("9901"));
            lastDataRegistered = 0;
            flag = 0;
            checkIfAllDataWasReceived();
        } else {
            startWriteCommand(gattCharacteristicWrite, checkSum("997F"));
            sendResults(lastResult);
        }
    }

    private List<String> splitEqually(String text) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + 48 - 1) / 48);

        for (int start = 0; start < text.length(); start += 48) {
            ret.add(text.substring(start, Math.min(text.length(), start + 48)));
        }
        return ret;
    }

    private void sendResults(String lastResult) {
        String oxygen = new BigInteger(lastResult.substring(38, 40), 16).toString();
        String pulseRate = new BigInteger(lastResult.substring(44, 46), 16).toString();

        Map<String, Object> data = new HashMap<>();
        data.put("oxygen", oxygen);
        data.put("pulse", pulseRate);
        data.put("pi", null);
        bluetoothConnection.onDataReceived(data, TypeBleDevices.SpO2.stringValue);
        BleManager.getInstance().disconnect(bleDevice);
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
