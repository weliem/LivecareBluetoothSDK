package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
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
import java.util.HashMap;
import java.util.Map;

public class ForaBPP20 {
    private final String turnOffDevice = "515000000000a3";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String readStorageNumberOfData = "512b01000000a3";
    private String clearMemoryCommand = "515201000000a3";
    private int dataCount;
    private int userId;

    public ForaBPP20(BluetoothConnection bluetoothConnection) {
        userId = 1;
        this.bluetoothConnection = bluetoothConnection;
        dataCount = -1;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("00001523")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startNotify(characteristic);
                }
            }
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristic, Utils.setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).length() > 4) {
                            calculateBPResult(HexUtil.formatHexString(data), characteristic);
                            if (HexUtil.formatHexString(data).substring(0, 4).equalsIgnoreCase("5133")) {
                                startWriteCommand(characteristic, Utils.checkSum(readStorageNumberOfData));
                            }
                            if (HexUtil.formatHexString(data).startsWith("512b")) {
                                String result = new BigInteger(HexUtil.formatHexString(data).substring(4, 6), 16).toString();
                                dataCount = Integer.parseInt(result);
                                if (dataCount == 0) {
                                    nextIteration(characteristic);
                                }
                            }
                            if (dataCount > 0) {
                                if (HexUtil.formatHexString(data).startsWith("512b") || HexUtil.formatHexString(data).startsWith("5126")) {
                                    startWriteCommand(characteristic, Utils.checkSum("5126" + "0000" + "000" + userId + "a3")); //Read the storage data with index(result)
                                }
                            }
                            if (HexUtil.formatHexString(data).startsWith("5152")) { //delete memory
                                startWriteCommand(characteristic, Utils.checkSum(turnOffDevice));

                                new Handler(Looper.getMainLooper()).postDelayed(() -> finishActivity(), 2000);
                            }
                        }
                    }
                });
    }

    private void calculateBPResult(String value, BluetoothGattCharacteristic characteristic) {
        if (value.startsWith("5126")) {
            String resultSys = new BigInteger(value.substring(4, 6), 16).toString();
            String resultDia = new BigInteger(value.substring(8, 10), 16).toString();
            String resultPulse = new BigInteger(value.substring(10, 12), 16).toString();

            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("sys", resultSys);
            objectMap.put("dia", resultDia);
            objectMap.put("pulse", resultPulse);
            objectMap.put("ahr", "");
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
            nextIteration(characteristic);

        } else if (value.startsWith("5154")) {
            startWriteCommand(characteristic, Utils.setDateTimeCommand());
        }
    }

    private void nextIteration(BluetoothGattCharacteristic characteristic) {
        if (userId < 4) {
            userId++;
            readStorageNumberOfData = "512b0" + userId + "000000a3";
            clearMemoryCommand = "51520" + userId + "000000a3";
            startWriteCommand(characteristic, Utils.checkSum(readStorageNumberOfData));
        } else {
            startWriteCommand(characteristic, Utils.checkSum(clearMemoryCommand));
        }
    }

    private void finishActivity() {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }
}
