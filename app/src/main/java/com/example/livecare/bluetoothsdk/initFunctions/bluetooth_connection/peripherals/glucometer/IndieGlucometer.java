package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
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
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.addZeroToHex;

public class IndieGlucometer {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private final String numberRecordsRequestCommand = "800FF00100000000000000000000000000000000";
    private final String numberRecordsResponseCommand = "800ff001";
    private final String recordsResponseCommand = "800ff002";
    private final String eraseAllMemoryRequestCommand = "800FF00300000000000000000000000000000000";
    private final String eraseAllMemoryResponseCommand = "800ff003";
    private String recordRequestCommand = "";
    private BluetoothGattCharacteristic characteristicWrite;
    private BluetoothGattCharacteristic characteristicNotify;
    private int recordNumberValue = 0;
    private Handler handler;
    private Runnable runnable;
    private final int delay = 2000;

    public IndieGlucometer(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("ffe0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        characteristicNotify = characteristic;
                    }
                }
            }
            if (service.getUuid().toString().contains("ffe5")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("ffe9")) {
                        characteristicWrite = characteristic;
                    }
                }
            }
        }

        if (characteristicWrite != null && characteristicNotify != null) {
            startNotifyGl(characteristicNotify);
        }
    }

    private void startNotifyGl(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristicWrite, setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).contains("800ff005")) {
                            //response from set Date
                            startWriteCommand(characteristicWrite, numberRecordsRequestCommand);
                        } else if (HexUtil.formatHexString(data).contains(numberRecordsResponseCommand)) {
                            String recordNumber = HexUtil.formatHexString(data).substring(10, 12);
                            if (recordNumber.equalsIgnoreCase("00")) {
                                recordNumber = "00";
                            } else {
                                recordNumber = "01";
                            }
                            recordNumberValue = Integer.parseInt(recordNumber);
                            recordRequestCommand = "800FF00200" + recordNumber + "000000000000000000000000000000";
                            startWriteCommand(characteristicWrite, recordRequestCommand);
                            if (recordNumberValue == 0) {
                                finishActivity();
                            } else {
                                checkIfHistoricIsPassed();
                            }
                        } else if (HexUtil.formatHexString(data).contains(recordsResponseCommand)) {
                            if (recordNumberValue > 0) {
                                recordRequestCommand = "800FF00200" + addZeroToHex(recordNumberValue + "") + "000000000000000000000000000000";
                                startWriteCommand(characteristicWrite, recordRequestCommand);
                                calculateResults(HexUtil.formatHexString(data));
                            }
                            recordNumberValue--;
                        } else if (HexUtil.formatHexString(data).contains(eraseAllMemoryResponseCommand)) {
                            finishActivity();
                        }
                    }
                });
    }


    private void checkIfHistoricIsPassed() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (recordNumberValue > 0) {
                    recordRequestCommand = "800FF00200" + addZeroToHex(recordNumberValue + "") + "000000000000000000000000000000";
                    startWriteCommand(characteristicWrite, recordRequestCommand);
                    handler.postDelayed(this, delay);
                } else {
                    startWriteCommand(characteristicWrite, eraseAllMemoryRequestCommand);
                }
            }
        };
        handler.postDelayed(runnable, delay);

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


    private void calculateResults(String formatHexString) {
        String result = new BigInteger(formatHexString.substring(26, 30), 16).toString();
        Map<String, Object> dataValue = new HashMap<>();
        dataValue.put("bgValue", result);
        bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
    }

    private String setDateTimeCommand() {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        String year2 = String.valueOf(year).substring(2, 4);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hourD = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        String sendYear = Integer.toHexString(Integer.parseInt(year2));
        String sendMonth = Integer.toHexString(month + 1);
        String sendDay = Integer.toHexString(day);
        String sendHour = Integer.toHexString(hourD);
        String sendMin = Integer.toHexString(min);
        String sendSec = Integer.toHexString(sec);

        String setTimeDate = "800FF005" + addZeroToHex(sendYear) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec) + " 00000000000000000000";
        return setTimeDate.toUpperCase();
    }

    private void finishActivity() {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
