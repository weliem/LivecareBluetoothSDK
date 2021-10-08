package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

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

public class ForaScale {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private final String readStorageNumberOfData = "512b01000000a3";
    private final String turnOffDevice = "515000000000a3";
    private int dataCount;
    private int i = 0;
    private String fullResult = "";

    public ForaScale(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
        dataCount = -1;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("00001523")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startNotifyScale(characteristic);
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                    }
                });
    }

    private void startNotifyScale(BluetoothGattCharacteristic characteristic) {
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
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).substring(0, 4).equalsIgnoreCase("5133")) {
                            startWriteCommand(characteristic, Utils.checkSum(readStorageNumberOfData));
                        }
                        if (HexUtil.formatHexString(data).startsWith("512b")) {
                            String result = new BigInteger(HexUtil.formatHexString(data).substring(4, 6), 16).toString();
                            dataCount = Integer.parseInt(result);
                            if (dataCount == 0) {
                                finishActivity();
                            }
                        }
                        if (dataCount > 0) {
                            mergeScaleResult(HexUtil.formatHexString(data), characteristic);
                            if (HexUtil.formatHexString(data).startsWith("512b") || HexUtil.formatHexString(data).startsWith("a5")) {
                                if (HexUtil.formatHexString(data).startsWith("a5")) {
                                    dataCount--;
                                }
                                startWriteCommand(characteristic, Utils.checkSum("517102" + "0000" + "a3")); //Read the storage data with index(Time)
                            }
                        }
                        if (HexUtil.formatHexString(data).startsWith("5152")) {
                            startWriteCommand(characteristic, Utils.checkSum(turnOffDevice));
                            new Handler(Looper.getMainLooper()).postDelayed(() -> finishActivity(), 2000);
                        }
                    }
                });
    }

    private void mergeScaleResult(String value, BluetoothGattCharacteristic characteristic) {

        if (value.startsWith("5171") || i > 0) {
            i++;
            fullResult = fullResult + value;
            if (i == 3) {
                i = 0;
                try {
                    calculateScaleResult(fullResult, characteristic);
                    fullResult = "";
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void calculateScaleResult(String value, BluetoothGattCharacteristic characteristic) {
        int weight = Integer.parseInt(new BigInteger(value.substring(36, 40), 16).toString());
        float weightToSend = (float) weight / 10;

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("weight", weightToSend);

        bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
        String clearMemoryCommand = "515201000000a3";
        startWriteCommand(characteristic, Utils.checkSum(clearMemoryCommand));
    }

    private String setDateTimeCommand() {
        Date date = new Date(); // your date
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
