package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleReadCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.pair;

public class CareSensGlucometer {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String lastResult = "";
    private BluetoothGattCharacteristic characteristicCustom;
    private BluetoothGattCharacteristic characteristicRACP;
    private BluetoothGattCharacteristic characteristicMeasurement;
    private BluetoothGattCharacteristic characteristicFirmware;
    private boolean firstTimeConnection = false;

    public CareSensGlucometer(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00002a52")) { //Record Access Control Point
                    characteristicRACP = characteristic;
                }
                if (characteristic.getUuid().toString().contains("00002a18")) { //Measurement
                    characteristicMeasurement = characteristic;
                }

                if (characteristic.getUuid().toString().contains("c4dea3bc")) { //Custom
                    characteristicCustom = characteristic;
                }

                if (characteristic.getUuid().toString().contains("00002a26")) { //Firmware Revision
                    characteristicFirmware = characteristic;
                }
            }
        }

        startRead(characteristicFirmware);
    }

    private void startRead(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().read(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        if(HexUtil.formatHexString(data).equals("3137352e3131302e312e31")){
                            //pin needed
                            if (checkPairedDevices(bleDevice.getMac())){
                                startNotifyCustom(characteristicCustom);
                            } else {
                                if (Utils.isNotOtherBleDeviceConnected()) {
                                    firstTimeConnection = true;
                                    bluetoothConnection.setPin(bleDevice.getDevice(), 1);
                                } else {
                                    Utils.teleHealthScanBroadcastReceiver(true);
                                    BleManager.getInstance().disconnect(bleDevice);
                                }
                            }
                        }else {
                            //no pin needed
                            if (checkPairedDevices(bleDevice.getMac())) {
                                startNotifyMeasurementNoPin(characteristicMeasurement);
                            } else {
                                firstTimeConnection = true;
                                pair(bleDevice.getDevice());
                                new Handler(Looper.getMainLooper()).postDelayed(() ->  startNotifyMeasurementNoPin(characteristicMeasurement), 4000);
                            }
                        }
                    }

                    @Override
                    public void onReadFailure(BleException exception) {

                    }
                });

    }

    private void startNotifyMeasurementNoPin(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startIndicateNoPin(characteristicRACP);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        lastResult = HexUtil.formatHexString(data);
                    }
                });
    }

    private void startIndicateNoPin(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {
                        startNotifyCustomNoPin(characteristicCustom);
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if(HexUtil.formatHexString(data).startsWith("06")){
                            if(firstTimeConnection){
                                BleManager.getInstance().disconnect(bleDevice);
                            }else {
                                calculateResults(lastResult);
                            }
                        }
                    }
                });
    }

    private void startNotifyCustomNoPin(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristic, "C002E10100");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if(HexUtil.formatHexString(data).startsWith("c002")){
                            startWriteCommand(characteristic, setDateTimeCommand());
                        }
                        if(HexUtil.formatHexString(data).startsWith("C003")){
                            startWriteCommand(characteristicRACP, "0401");
                        }

                        if(HexUtil.formatHexString(data).startsWith("0500")){
                            startWriteCommand(characteristicRACP, "0106");
                        }
                    }
                });
    }

    private void startNotifyMeasurement(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristicRACP, "0106");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        lastResult = HexUtil.formatHexString(data);
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        if (formatHexString.length() > 27) {
            if (formatHexString.startsWith("0208", 24) || formatHexString.startsWith("FE07", 24)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    BleManager.getInstance().disconnect(bleDevice);
                }, 3000);

            } else {
                String result;
                final String result1 = new BigInteger(formatHexString.substring(24, 26), 16).toString();
                if (formatHexString.startsWith("b0", 26)) {
                    result = result1;
                } else {
                    int resultAbove255 = Integer.parseInt(result1) + 256;
                    result = String.valueOf(resultAbove255);
                }

                Map<String, Object> dataValue = new HashMap<>();
                dataValue.put("bgValue", result);
                bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue);
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }

    private void startNotifyCustom(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        Utils.teleHealthScanBroadcastReceiver(true);
                        startWriteCommand(characteristic, setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).startsWith("0500")) {
                            startIndicate(characteristicRACP);
                        }
                    }
                });
    }

    private void startIndicate(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {
                        startWriteCommand(characteristicRACP, "0401");
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).startsWith("05")) {//05001000
                            startNotifyMeasurement(characteristicMeasurement);
                        } else if (HexUtil.formatHexString(data).startsWith("06")) {
                            if(firstTimeConnection){
                                BleManager.getInstance().disconnect(bleDevice);
                            }else {
                                calculateResults(lastResult);
                            }
                        }
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {

                    }
                });
    }

    private String setDateTimeCommand() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hourD = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        String sendYear = Integer.toHexString(year);
        String sendMonth = Integer.toHexString(month + 1);
        String sendDay = Integer.toHexString(day);
        String sendHour = Integer.toHexString(hourD);
        String sendMin = Integer.toHexString(min);
        String sendSec = Integer.toHexString(sec);

        String setTimeDate = "C0030100" + convertBigEndian(sendYear) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec);
        System.out.println("setTimeDate: " + setTimeDate.toUpperCase());
        return setTimeDate.toUpperCase();
    }

    private String convertBigEndian(String str) {
        if (str.length() == 3) {
            str = "0" + str;
        }
        return str.substring(2, 4) + str.substring(0, 2);
    }

    private String addZeroToHex(String str) {
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    public void startNotifyCustom() {
        startNotifyCustom(characteristicCustom);
    }
}
