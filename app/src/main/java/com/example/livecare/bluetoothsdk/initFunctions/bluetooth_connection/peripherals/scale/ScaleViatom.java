package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Constants;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_VIATOM;

public class ScaleViatom {

    private BluetoothLeScanner scanner;
    private BleDevice bleDevice;
    private final LiveCareMainClass liveCareMainClass;
    private boolean sendData = true;

    public ScaleViatom(LiveCareMainClass liveCareMainClass, BleDevice bleDevice) {
        this.liveCareMainClass = liveCareMainClass;
        this.bleDevice = bleDevice;
        Constants.ViatomScaleConnected = true;
        scanWithFilter();
        Utils.teleHealthScanBroadcastReceiver(true);
    }

    private void scanWithFilter(){
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setReportDelay(0)
                .build();


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter!=null){
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(null, scanSettings, scanCallback);
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if(device.getName()!=null){
                if(device.getAddress().equals(bleDevice.getMac())){


                    String value;
                    if(bleDevice.getName().equals(BLE_SCALE_VIATOM)){
                         value = getByteString(result.getScanRecord().getBytes());
                    }else {
                        value = getByteStringSMG4(result.getScanRecord().getBytes());
                    }

                    if(!value.substring(4,12).equals("00000000")){
                        if(sendData){
                            calculateResult(value.substring(0,4));
                            sendData = false;
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {}

        @Override
        public void onScanFailed(int errorCode) {}
    };

    private void calculateResult(String substring) {
        int weight = Integer.parseInt(substring, 16);
        float weightLbs = (float) (weight * 0.1 * 2.20462);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("weight", weightLbs);
        liveCareMainClass.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
        Constants.currentTimeForLastTelehealthServiceScale = Calendar.getInstance().getTime().getTime();
        onDestroy();
    }

    private String getByteString(byte[] b) {
        char[] HEX = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer(300);

        int bb = b[25]  & 0xFF;
        sb.append(HEX[bb >> 4]);
        sb.append(HEX[bb & 0x0F]);

        int bbb = b[26]  & 0xFF;
        sb.append(HEX[bbb >> 4]);
        sb.append(HEX[bbb & 0x0F]);

        int bbbb = b[27]  & 0xFF;
        sb.append(HEX[bbbb >> 4]);
        sb.append(HEX[bbbb & 0x0F]);

        int c = b[28]  & 0xFF;
        sb.append(HEX[c >> 4]);
        sb.append(HEX[c & 0x0F]);

        int d = b[29]  & 0xFF;
        sb.append(HEX[d >> 4]);
        sb.append(HEX[d & 0x0F]);

        int e = b[30]  & 0xFF;
        sb.append(HEX[e >> 4]);
        sb.append(HEX[e & 0x0F]);

        return sb.toString();
    }

    private String getByteStringSMG4(byte[] b) {
        char[] HEX = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer(300);

        int bb = b[23]  & 0xFF;
        sb.append(HEX[bb >> 4]);
        sb.append(HEX[bb & 0x0F]);

        int bbb = b[24]  & 0xFF;
        sb.append(HEX[bbb >> 4]);
        sb.append(HEX[bbb & 0x0F]);

        int bbbb = b[25]  & 0xFF;
        sb.append(HEX[bbbb >> 4]);
        sb.append(HEX[bbbb & 0x0F]);

        int c = b[26]  & 0xFF;
        sb.append(HEX[c >> 4]);
        sb.append(HEX[c & 0x0F]);

        int d = b[27]  & 0xFF;
        sb.append(HEX[d >> 4]);
        sb.append(HEX[d & 0x0F]);

        int e = b[28]  & 0xFF;
        sb.append(HEX[e >> 4]);
        sb.append(HEX[e & 0x0F]);

        return sb.toString();
    }

    public void onDestroy() {
        Constants.ViatomScaleConnected = false;
        scanner.stopScan(scanCallback);
    }
}
