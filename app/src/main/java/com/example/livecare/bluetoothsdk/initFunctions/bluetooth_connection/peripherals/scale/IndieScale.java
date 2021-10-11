package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleReadCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.addZeroToHex;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.convertBigEndian;

public class IndieScale {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic weightCharacteristic;
    private int flag = 0;
    private int origin;
    private Handler handler;
    private Runnable runnable;
    private int dataSendCount = 0;
    private double sendResultLb = 0;

    public IndieScale(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt, int origin) {
        this.origin = origin;
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {

            if (service.getUuid().toString().contains("00001805")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startWriteCommand(characteristic, setDateTimeCommand());
                }
            } else if (service.getUuid().toString().contains("0000181d")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    weightCharacteristic = characteristic;
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
                        startRead(characteristic);
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void startRead(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().read(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        checkIfDataWasSent();
                        startIndicate(weightCharacteristic);
                    }

                    @Override
                    public void onReadFailure(BleException exception) {}
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
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }


    private void calculateResults(String formatHexString) {
        String convertResult = formatHexString.substring(4, 6) + formatHexString.substring(2, 4);
        String result = new BigInteger(convertResult, 16).toString();

        double sendResultKg;
        if (origin == 1) {
            sendResultKg = (Double.parseDouble(result) * 0.005);
            sendResultLb = (sendResultKg * 2.205);
        } else {
            sendResultKg = (Double.parseDouble(result) * 0.01);
            sendResultLb = sendResultKg;
        }
        flag++;
    }


    private void checkIfDataWasSent() {
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            if (dataSendCount != 0) {
                if (dataSendCount == flag) {
                    BleManager.getInstance().disconnect(bleDevice);
                    if (sendResultLb != 0) {
                        updateScaleResultToFireBase(new DecimalFormat("##.#").format(sendResultLb));
                    }
                } else {
                    dataSendCount = flag;
                    handler.postDelayed(runnable, 1000);
                }
            } else {
                dataSendCount = flag;
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void updateScaleResultToFireBase(String weight) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("weight", weight);
        bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
    }

    private String setDateTimeCommand() {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hourD = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        String sendYear1 = Integer.toHexString(year);
        String sendMonth = Integer.toHexString(month + 1);
        String sendDay = Integer.toHexString(day);
        String sendHour = Integer.toHexString(hourD);
        String sendMin = Integer.toHexString(min);
        String sendSec = Integer.toHexString(sec);
        String sendDayOfWeek = Integer.toHexString(dayOfWeek);
        String setTimeDate = convertBigEndian(sendYear1) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec)
                + addZeroToHex(sendDayOfWeek) + "0000";
        return setTimeDate.toUpperCase();
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
