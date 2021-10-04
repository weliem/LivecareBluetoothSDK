package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spirometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.CountDownTimer;
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

public class SpirometerAndesFit {
    private final String TAG = "SpirometerAndesFit";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicNotify;
    private BluetoothGattCharacteristic characteristicWrite;
    private CountDownTimer countDownTimer;

    public SpirometerAndesFit(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if(characteristic.getUuid().toString().startsWith("0000ff0a")){
                        characteristicNotify = characteristic;
                    }
                    if(characteristic.getUuid().toString().startsWith("0000ff0b")){
                        characteristicWrite = characteristic;
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
                        setCountDownTimer();
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String value = HexUtil.formatHexString(data);
                        if(value.startsWith("aa0600")){
                            countDownTimer.cancel();
                            startWrite(characteristicWrite,"5501");
                        }
                        if(value.startsWith("aa01")){
                            String resultNr = value.substring(6,8);
                            startWrite(characteristicWrite,"5502"+resultNr+"00");
                        }
                        if(value.startsWith("dd")){
                            //dd 802a03600e014501a52a03609801a101 417 4.08 //dd a11203609202ef01
                            if(value.length() == 18){
                                calculateResults(value,1);
                            }else if(value.length() == 34){
                                calculateResults(value,2);
                            }
                        }
                        if(value.contains("aa0300")){
                            BleManager.getInstance().disconnect(bleDevice);
                        }
                    }
                });
    }

    private void calculateResults(String value, int i) {
        String resultFEV1;
        String resultPEF;
        if(i == 1){
             resultFEV1 = new BigInteger(value.substring(12, 14) + value.substring(10, 12), 16).toString();
             resultPEF = new BigInteger(value.substring(16, 18) + value.substring(14, 16), 16).toString();
        }else {
             resultFEV1 = new BigInteger(value.substring(28, 30) + value.substring(26, 28), 16).toString();
             resultPEF = new BigInteger(value.substring(32, 34) + value.substring(30, 32), 16).toString();
        }

        double fev1 = Double.parseDouble(resultFEV1);
        double fev1Result = fev1/100;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("fev1", fev1Result);
        dataMap.put("pef", resultPEF);

        bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.Spirometer.stringValue);
        startWrite(characteristicWrite,"5503");
    }


    private void setCountDownTimer() {
        countDownTimer = new CountDownTimer(600100, 500) {
            @Override
            public void onTick(long l) {
                startWrite(characteristicWrite,"5506");
            }

            @Override
            public void onFinish() {
                countDownTimer.cancel();
            }
        };
        countDownTimer.start();
    }

    private void startWrite(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),//turn Off device
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {

                    }
                });
    }

    public void onDestroy() {
        countDownTimer.cancel();
    }
}
