package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

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

public class LiveCareBP {
    private Map<String, Object> dataMap;
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private final String startCommand = "FDFDFA050D0A";
    private boolean calculatedData = false;
    private boolean writeEndCommandError = false;
    private BluetoothGattCharacteristic characteristicWrite;
    private BluetoothGattCharacteristic characteristicNotify;
    private final String endCommand = "fdfdfa060d0a";

    public LiveCareBP(BluetoothConnection bluetoothConnection,BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        getBluetoothGattCharacteristic(gatt);
    }

    private void getBluetoothGattCharacteristic(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0-0000-1000-8000-00805f9b34fb")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("0000fff2")) {
                        characteristicWrite = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("0000fff1")) {//0000fff1-0000-1000-8000-00805f9b34fb
                        characteristicNotify = characteristic;
                    }
                }
            }
        }
        if (characteristicWrite != null && characteristicNotify != null) {
            startNotifyBP(characteristicNotify);
        }
    }

    private void startNotifyBP(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {}

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String value = HexUtil.formatHexString(data);
                        if(value != null){
                            if (("a5").equalsIgnoreCase(value)) {
                                startWrite(characteristicWrite, startCommand);
                            } else {
                                calculateResults(value);
                            }
                        }else {
                            finishActivity("onFail");
                        }
                    }
                });
    }

    private void calculateResults(String value) {
        if (value.length() > 5) {
            String BPResultsError = "fdfdfd";
            if (value.substring(0, 6).equalsIgnoreCase(BPResultsError)) {
                writeEndCommandError = false;
                startWrite(characteristicWrite, endCommand);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!writeEndCommandError) {
                        finishActivity("writeEndCommandError");
                    }
                }, 3000);
            }

            if (value.length() == 16 || value.length() == 32) {
                String BPResults = "fdfdfc";
                if (value.substring(0, 6).equalsIgnoreCase(BPResults)) {
                    if (!calculatedData) {
                        String sys = new BigInteger(value.substring(6, 8), 16).toString();
                        String dia = new BigInteger(value.substring(8, 10), 16).toString();
                        String pul = new BigInteger(value.substring(10, 12), 16).toString();

                        dataMap = new HashMap<>();
                        dataMap.put("sys", sys);
                        dataMap.put("dia", dia);
                        dataMap.put("pulse", pul);
                        dataMap.put("ahr", "");
                        startWrite(characteristicWrite, setDateTimeCommand());

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
                            startWrite(characteristicWrite, endCommand);
                        }, 2000);
                    }
                    calculatedData = true;
                }
            }
        }
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

        String setTimeDate = "FDFDFA09" + addZeroToHex(sendYear) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec) + "0D0A";
        System.out.println("setTimeDate: " + setTimeDate.toUpperCase());
        return setTimeDate.toUpperCase();
    }

    private String addZeroToHex(String str) {
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    private void startWrite(BluetoothGattCharacteristic characteristic, final String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        if (HexUtil.formatHexString(justWrite).equals(endCommand)) {
                            writeEndCommandError = true;
                            finishActivity("dataSentSuccessfully");
                        }
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void finishActivity(String reason) {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
        if(!reason.equals("dataSentSuccessfully")){
            dataMap = new HashMap<>();
            dataMap.put("error",reason);
            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
        }
    }
}
