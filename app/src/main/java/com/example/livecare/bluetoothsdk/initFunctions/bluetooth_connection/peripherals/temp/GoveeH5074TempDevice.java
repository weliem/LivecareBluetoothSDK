package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp;

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
import java.util.HashMap;
import java.util.Map;

public class GoveeH5074TempDevice {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristic1;
    private BluetoothGattCharacteristic characteristicNotify2;
    private double tempIn = 0;
    private double humidityIn = 0;
    private double tempOut = 0;
    private double humidityOut = 0;
    private boolean notifyStarted = false;

    public GoveeH5074TempDevice(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("434b535f2011")) {
                    characteristic1 = characteristic;
                    startNotify(characteristic1);
                }
                if (characteristic.getUuid().toString().contains("434b535f2012")) {
                    characteristicNotify2 = characteristic;
                }
            }
        }
    }

    private void isNotifyStarted(){
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
          if(!notifyStarted){
              startWriteCommand(characteristic1, "3320000000000000000000000000000000000013");
          }
        }, 2000);
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        isNotifyStarted();
                        startWriteCommand(characteristic, "3320000000000000000000000000000000000013");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        notifyStarted = true;
                        if(HexUtil.formatHexString(data).startsWith("3320")){
                            startNotifyResult(characteristicNotify2);
                        }
                    }
                });
    }


    private void startNotifyResult(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristic, "aa010000000000000000000000000000000000ab");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if(HexUtil.formatHexString(data).startsWith("aa01")){
                            String temp = new BigInteger(HexUtil.formatHexString(data).substring(4, 8), 16).toString();
                            String humidity = new BigInteger(HexUtil.formatHexString(data).substring(8, 12), 16).toString();

                            tempIn = Integer.parseInt(temp) * 0.01;
                            humidityIn = Integer.parseInt(humidity) * 0.01;
                            new Handler(Looper.getMainLooper()).postDelayed(() -> startNotifyOutDoor(characteristic1), 1000);
                        }
                    }
                });
    }

    private void startNotifyOutDoor(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristic, "3320010000000000000000000000000000000012");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if(HexUtil.formatHexString(data).startsWith("3320")){
                            startNotifyResultOutDoor(characteristicNotify2);
                        }
                    }
                });
    }


    private void startNotifyResultOutDoor(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristic, "aa010000000000000000000000000000000000ab");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if(HexUtil.formatHexString(data).startsWith("aa01")){
                            String temp = new BigInteger(HexUtil.formatHexString(data).substring(4, 8), 16).toString();
                            String humidity = new BigInteger(HexUtil.formatHexString(data).substring(8, 12), 16).toString();

                            tempOut = Integer.parseInt(temp) * 0.01;
                            humidityOut = Integer.parseInt(humidity) * 0.01;

                            double fahrenheitIn = 32 + (tempIn * 9 / 5);
                            double fahrenheitOut = 32 + (tempOut * 9 / 5);

                            Map<String, Object> objectMap = new HashMap<>();
                            objectMap.put("tempIn", fahrenheitIn);
                            objectMap.put("humidityIn", humidityIn);
                            objectMap.put("tempOut", fahrenheitOut);
                            objectMap.put("humidityOut", humidityOut);
                            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Temp.stringValue);
                            BleManager.getInstance().disconnect(bleDevice);
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
}
