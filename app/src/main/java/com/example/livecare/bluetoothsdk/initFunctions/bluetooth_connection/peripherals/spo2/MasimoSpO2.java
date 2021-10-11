package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.util.HashMap;
import java.util.Map;

public class MasimoSpO2 {
    private final BluetoothConnection bluetoothConnection;
    private final BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicWrite;
    private BluetoothGattCharacteristic characteristicNotify;
    private int i = 0;
    private String finalFormatHexString;
    private int spO2Min = Integer.MAX_VALUE;
    private int spO2Max = -1;
    private int spO2Avg = 0;
    private int pRMin = Integer.MAX_VALUE;
    private int pRMax = -1;
    private int pRAvg = 0;

    public MasimoSpO2(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    public void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("54c21000")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("54c21001")) {
                        characteristicWrite = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("54c21002")) {
                        characteristicNotify = characteristic;
                    }
                }
            }
        }
        startNotify(characteristicNotify);
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristicWrite, "77020107");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).contains("77140142101f000301001700")) {
                            startWriteCommand(characteristicWrite, "7705031f0003d6");
                        } else if (HexUtil.formatHexString(data).contains("7703fe03cb")) {
                            startWriteCommand(characteristicWrite, "77060205831d60a5");
                        } else if (HexUtil.formatHexString(data).contains("771105")) {
                            calculateResults(HexUtil.formatHexString(data));
                        } else if (HexUtil.formatHexString(data).contains("03fe07d7")) {
                            BleManager.getInstance().disconnect(bleDevice);
                        }
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        String spO2Hex = formatHexString.substring(16, 18);
        String pRHex = formatHexString.substring(20, 22);
        int spO2 = Integer.parseInt(spO2Hex, 16);
        int pR = Integer.parseInt(pRHex, 16);
        if (spO2 == 255) {
            spO2 = 0;
        }
        if (pR == 255) {
            pR = 0;
        }
        if (spO2 > 60 && pR > 40) {
            if (spO2Max < spO2) {
                spO2Max = spO2;
            }
            if (spO2Min > spO2) {
                spO2Min = spO2;
            }
            spO2Avg = spO2Avg + spO2;
            if (pRMax < pR) {
                pRMax = pR;
            }
            if (pRMin > pR) {
                pRMin = pR;
            }
            pRAvg = pRAvg + pR;
            finalFormatHexString = finalFormatHexString + formatHexString;
            i++;
        }
        if (spO2 == 0 && pR == 0 && i != 0) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("oxygen", spO2Avg / i);
            objectMap.put("oxygenMin", spO2Min);
            objectMap.put("oxygenMax", spO2Max);
            objectMap.put("pulse", pRAvg / i);
            objectMap.put("pulseMin", pRMin);
            objectMap.put("pulseMax", pRMax);
            objectMap.put("pi", null);
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.SpO2.stringValue);
            startWriteCommand(characteristicWrite, "77020715");
            i = 0;
        }
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                    }
                });
    }
}
