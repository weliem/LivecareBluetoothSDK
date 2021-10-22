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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ForaBPTNG {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private final String readStorageNumberofData = "512b01000000a3";
    private final String turnOffDevice = "515000000000a3";
    private int dataCount;
    private final int userId;

    public ForaBPTNG(BluetoothConnection bluetoothConnection) {
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
                        startWriteCommand(characteristic, setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).length() > 4) {
                            calculateBPResult(HexUtil.formatHexString(data), characteristic);
                            if (HexUtil.formatHexString(data).substring(0, 4).equalsIgnoreCase("5133")) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> startWriteCommand(characteristic, Utils.checkSum(readStorageNumberofData)), 3000);
                            }
                            if (HexUtil.formatHexString(data).startsWith("512b")) {
                                String result = new BigInteger(HexUtil.formatHexString(data).substring(4, 6), 16).toString();
                                dataCount = Integer.parseInt(result);
                                if (dataCount == 0) {
                                    startWriteCommand(characteristic, Utils.checkSum(turnOffDevice));
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> finishActivity(), 2000);
                                }
                            }
                            if (dataCount > 0) {
                                if (HexUtil.formatHexString(data).startsWith("512b")) {
                                    startWriteCommand(characteristic, Utils.checkSum("5126" + Utils.addZeroToHex(String.valueOf(dataCount - 1)) + "00" + "000" + userId + "a3")); //Read the storage data with index(result)
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
            String clearMemoryCommand = "515201000000a3";
            startWriteCommand(characteristic, Utils.checkSum(clearMemoryCommand));
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
        } else if (value.startsWith("5154")) {
            startWriteCommand(characteristic, setDateTimeCommand());
        }
    }

    private String setDateTimeCommand() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String sendHour = Integer.toHexString(cal.get(Calendar.HOUR_OF_DAY));
        String sendMin = Integer.toHexString(cal.get(Calendar.MINUTE));

        String setTimeDate = "5133" + Utils.convertIntToHex(cal) + Utils.addZeroToHex(sendMin) + Utils.addZeroToHex(sendHour) + "a3";
        return Utils.checkSum(setTimeDate);
    }

    private void finishActivity() {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }
}
