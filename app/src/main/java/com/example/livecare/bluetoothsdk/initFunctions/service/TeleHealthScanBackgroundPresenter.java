package com.example.livecare.bluetoothsdk.initFunctions.service;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.emergency_button.VAlertDevice;
import com.example.livecare.bluetoothsdk.initFunctions.enums.BleDevicesName;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Constants;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleScanCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.Calendar;
import java.util.List;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_AD_UA_651BLE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BC57;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BM54;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BM67;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_TRANSTEK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BP_WELLUE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_BP;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_INDIE_HEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_TNG_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_CARDIOBEAT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_ECG_IHEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_ECG_VIVALNK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_FITNESS_TRACKER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_ACCU_CHECK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_AGAMETRIX_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_AGAMETRIX_UnPaired;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_CARESENS_S;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_CONTOUR;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_INDIE_HEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_ONE_TOUCH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_PREMIUM_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_PREMIUM_FORA2;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_TAI_DOC;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_TRUE_METRIX;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_OMRON_BP1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_OMRON_BP2;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_OMRON_BP3;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_OMRON_BP4;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PRIZMA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_BERRYMED;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_BEURER_PO60;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FS2OF1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FS2OF2;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_IHEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_MASIMO;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_NONIN;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_TAI_DOC;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_RING_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_AD_UC_352BLE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_ARBOLEAF;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_INDIE_HEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_INDIE_HEALTH_SMALL;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_QN_SCALE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_SMG4;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SPIROMETER_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SPIROMETER_SMART_ONE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMPERATURE_SENSOR_GOVEE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_AET_WD;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_VICKS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_VIVALNK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_FORA_IR20;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_UNAAN;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_V_ALERT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.currentTimeForLastTelehealthService;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.currentTimeForLastTelehealthServiceTEMP;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.createBond;

public class TeleHealthScanBackgroundPresenter {

    private static final String TAG = "TeleHealthScan";
    private TeleHealthService teleHealthService;
    private long startTime = 0;
    private Handler handler;
    private boolean startScan;
    private BleDevice mDevice;
    private VAlertDevice vAlertDevice;
    private final BroadcastReceiver teleHealthScanBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("startScan", false)) {
                Log.d(TAG, "teleHealthScanBroadcastReceiver startScan = false");
                BleManager.getInstance().cancelScan();
                startScan = false;
            } else {
                Log.d(TAG, "teleHealthScanBroadcastReceiver startScan = true");
                startScan = true;
                startScan();
            }
        }
    };

    TeleHealthScanBackgroundPresenter(TeleHealthService teleHealthService) {
        this.teleHealthService = teleHealthService;
        startScan = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("teleHealthScan.BroadcastReceiver");
        teleHealthService.registerReceiver(teleHealthScanBroadcastReceiver, filter);
    }

    void iniFunction() {
        startTime = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());

        if (!"Link+".equalsIgnoreCase(BleManager.getInstance().getBluetoothAdapter().getName())) {
            BleManager.getInstance().getBluetoothAdapter().setName("Link+");
        }

        startScan();
        if (teleHealthService != null) {
            notifyScanStarted(teleHealthService);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        teleHealthService.registerReceiver(mReceiver, filter);
    }

    private void notifyScanStarted(Context context) {
        Intent local = new Intent();
        local.setAction("scan.started");
        context.sendBroadcast(local);
    }

    private void startScan() {
        if (BleManager.getInstance().getScanSate().getCode() != 1 && BleManager.getInstance().isBleStateOn()) {
            BleManager.getInstance().scan(new BleScanCallback() {
                @Override
                public void onScanStarted(boolean success) {
                    Log.d(TAG, "onScanStarted scan started: "+success);
                    if (!success) {
                        Utils.resetTeleHealthService();
                    }else {
                        updateScanningStage("onScanStarted");
                    }
                }

                @Override
                public void onLeScan(BleDevice bleDevice) {
                    super.onLeScan(bleDevice);
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    //Log.d(TAG, "onScanning: " + bleDevice.getName() + " mac " + bleDevice.getMac() + " start time " + startTime);
                    scanDevicesResponse(bleDevice);
                }

                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    Log.d(TAG, "onScanFinished: ");
                    updateScanningStage("onScanFinished");
                    if (scanResultList.isEmpty() && teleHealthService != null && !isAnyDeviceConnected()) {
                        teleHealthService.resetBluetooth();
                    } else {
                        if (startScan) {
                            startScan();
                        }
                    }
                }
            });
        } else {
            Utils.resetTeleHealthService();
        }
    }

    private void updateScanningStage(String stage){
        if(teleHealthService!=null){
            Intent local = new Intent();
            local.setAction("update.scanning.stage");
            local.putExtra("onScan", stage);
            teleHealthService.sendBroadcast(local);
        }
    }

    private boolean isAnyDeviceConnected() {
        if (!Utils.isAnyBleDeviceConnected()) {
           // if (BleManagerViVaTemp.mInstance == null) {
              //  if (VitalClient.getInstance() == null || !VitalClient.getInstance().isInitial) {
                    return Utils.isAnyBleDeviceConnecting();
               // }
           // }
        }//else {
            //if(Utils.isNotForegroundBleDeviceConnected()){
               // if(teleHealthService!=null){
                   // teleHealthService.checkIfBackGroundIHealthIsConnected();
               // }
           // }
        //}
        return true;
    }

    private void scanDevicesResponse(BleDevice device) {
        String iHealthDevices = "1";//all iHealth devices will go with this flag to ScanActivity
        String otherDevices = "2";//all other devices will go with this flag to ScanActivity
        String sdkDevices = "3";//VivaLNK devices will go with this flag to ScanActivity
        String fitnessDevices = "4";//fitnessDevices devices will go with this flag to ScanActivity

        if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthService < 5000) {
            return;
        }
        if (device.getName() != null) {
            Log.d(TAG, "scanDevicesResponse: "+ device.getName());
            switch (device.getName()) {
                case BLE_BlOOD_PRESSURE_BP:
                case BLE_BlOOD_PRESSURE_ANDES_FIT:
                case BLE_BLOOD_PRESSURE_TRANSTEK:
                case BLE_BLOOD_PRESSURE_JUMPER:
                    if ((Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceBP > 20000)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.BP.stringValue, device, otherDevices);
                    }
                    break;

                case BLE_BlOOD_PRESSURE_INDIE_HEALTH:
                case BLE_BLOOD_PRESSURE_FORA:
                case BLE_BlOOD_PRESSURE_TNG_FORA:
                case BLE_BP_WELLUE:
                case BLE_BLOOD_PRESSURE_BEURER_BM67:
                case BLE_BLOOD_PRESSURE_BEURER_BC57:
                case BLE_BLOOD_PRESSURE_BEURER_BM54:
                case BLE_BLOOD_PRESSURE_CVS:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices);
                    break;

                case BLE_GLUCOMETER_PREMIUM_FORA:
                case BLE_GLUCOMETER_INDIE_HEALTH:
                case BLE_GLUCOMETER_PREMIUM_FORA2:
                case BLE_GLUCOMETER_TAI_DOC:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                    break;

                case BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS:
                case BLE_GLUCOMETER_TRUE_METRIX:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceGL > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                    }
                    break;

                case BLE_PULSE_OXIMETER_BERRYMED:
                case BLE_PULSE_OXIMETER_ANDES_FIT:
                case BLE_PULSE_OXIMETER_FS2OF1:
                case BLE_PULSE_OXIMETER_FS2OF2:
                case BLE_PULSE_OXIMETER_FORA:
                case BLE_PULSE_OXIMETER_MASIMO:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceSpO2 > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices);
                    }
                    break;

                case BLE_PULSE_OXIMETER_TAI_DOC:
                case BLE_PULSE_OXIMETER_BEURER_PO60:
                case BLE_PULSE_OXIMETER_JUMPER:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices);
                    break;

                case BLE_PULSE_OXIMETER_IHEALTH:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, iHealthDevices);
                    break;

                case BLE_SCALE_ANDES_FIT:
                case BLE_SCALE_INDIE_HEALTH_SMALL:
                case BLE_SCALE_JUMPER:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceScale > 20000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, otherDevices);
                    }
                    break;

                case BLE_SCALE_INDIE_HEALTH:
                case BLE_SCALE_FORA:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, otherDevices);
                    break;

                case BLE_SCALE_SMG4:
                case BLE_SCALE_VIATOM:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceScale > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices);
                    }
                    break;

                case BLE_TEMP_ANDES_FIT:
                    if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 70000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    }
                    break;

                case BLE_TEMP_AET_WD:
                case BLE_THERMOMETER_FORA_IR20:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    break;

                case BLE_THERMOMETER_VIATOM:
                    if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 80000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    }
                    break;

                case BLE_THERMOMETER_UNAAN:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    }
                    break;

                case BLE_ECG_IHEALTH:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, iHealthDevices);
                    break;

                case BLE_CARDIOBEAT:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, otherDevices);
                    break;

                case BLE_SCALE_ARBOLEAF:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthService > 10000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices);
                    }
                    break;

             /*   case iHealthDevicesManager.TYPE_PT3SBT:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForPT3SBT > 600000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, iHealthDevices, TypeBleDevices.Temp.stringValue,false);
                    }
                    break;*/

                case BLE_SPIROMETER_ANDES_FIT:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForSpirometer > 100000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Spirometer.toString(), device, otherDevices);
                    }
                    break;

                case BLE_TEMP_VICKS:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 60000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    }
                    break;

                    default:
                    /*if (device.getName().contains("iHealth HS4S")) {
                        if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceHS4S > 60000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue,false);
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_TS28B)) {
                        if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceTS28B > 40000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, iHealthDevices, TypeBleDevices.Temp.stringValue,false);
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_BG5S)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, iHealthDevices, TypeBleDevices.Gl.stringValue, PrefManager.isBooleanSet(enable_background_connection));
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_HS2)) {
                        if(device.getName().startsWith(iHealthDevicesManager.TYPE_HS2S)){
                            decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue, PrefManager.isBooleanSet(enable_background_connection));
                        }else {
                            if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceHS2 > 45000) {
                                decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue,false);
                            }
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_550BT)) {
                        if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceKN550BT > 15000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, iHealthDevices, TypeBleDevices.BP.stringValue, false);
                        }
                    } else */if (device.getName().contains(BLE_OMRON_BP1) || device.getName().contains(BLE_OMRON_BP2) ||
                            device.getName().contains(BLE_OMRON_BP3) || device.getName().contains(BLE_OMRON_BP4)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices);

                    } else if (device.getName().startsWith(BLE_GLUCOMETER_CARESENS_S)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                    } else if (device.getName().contains(BLE_GLUCOMETER_CONTOUR)) {
                        if (checkPairedDevices(device.getMac())) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                        } else {
                            try {
                                if (createBond(device.getDevice())) {
                                    handler.postDelayed(() -> decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices), 2000);
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    } else if (device.getName().contains(BLE_GLUCOMETER_ACCU_CHECK)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                    } else if (device.getName().contains(BLE_ECG_VIVALNK)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, sdkDevices);
                    } else if (device.getName().contains(BLE_PRIZMA)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.PRIZMA.toString(), device, otherDevices);
                    } else if (device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_CVS) || device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_UnPaired)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                    } else if (device.getName().contains(BLE_GLUCOMETER_ONE_TOUCH)) {
                        if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceGL > 120000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices);
                        }
                    } else if (device.getName().contains(BLE_TEMP_JUMPER) || device.getName().contains(BLE_TEMP_JUMPER1)) {
                        if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0
                                && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 70000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                        }
                    } else if (device.getName().contains(BLE_TEMPERATURE_SENSOR_GOVEE)) {
                        // TODO: 10/4/2021
                        // decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices);
                    } else if (device.getName().contains(BLE_V_ALERT)) {
                        if (!BleManager.getInstance().isConnected(device) && teleHealthService != null) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.V_ALERT.toString(), device, otherDevices);
                        }
                    } else if (device.getName().contains(BLE_SPIROMETER_SMART_ONE)) {
                        teleHealthService.broadcastBluetoothFragment("display_spirometer","true");
                    } else if (device.getName().startsWith(BLE_PULSE_OXIMETER_NONIN)) {
                        if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 &&
                                Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceSpO2 > 30000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices);
                        }
                    } else if (device.getName().contains(BLE_BLOOD_PRESSURE_AD_UA_651BLE)) {
                        if (mDevice != null){
                            return;
                        }
                        mDevice = device;
                        if (checkPairedDevices(device.getMac())) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices);
                        } else {
                            try {
                                createBond(device.getDevice());
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    }else if (device.getName().contains(BLE_SCALE_AD_UC_352BLE)) {
                        if (mDevice != null){
                            return;
                        }
                        mDevice = device;
                        if (checkPairedDevices(device.getMac())) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, otherDevices);
                        } else {
                            try {
                                createBond(device.getDevice());
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    } else if (device.getName() != null && device.getName().contains(BLE_RING_VIATOM)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices);
                    } else if (device.getName() != null && device.getName().contains(BLE_FITNESS_TRACKER)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Fitness.toString(), device, otherDevices);
                    }
            }
        } else {
            if (device.getMac().contains(BLE_TEMP_VIVALNK)) {
                decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, sdkDevices);
            } else if (device.getMac().contains(BLE_SCALE_QN_SCALE)) {
                if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthService > 10000) {
                    decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices);
                }
            }
        }
    }

    public void resumeScan() {
        startScan = true;
        startScan();
    }

    private void decisionFunctionAfterGettingBTMac(String deviceName, BleDevice device, String iHealthOrOtherDevice) {
        if (vAlertDevice != null) {
            vAlertDevice.disconnect();
            vAlertDevice = null;
        }
        if (teleHealthService != null) {
            teleHealthService.setDeviceFound(deviceName, device, iHealthOrOtherDevice);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch(state){
                    case BluetoothDevice.BOND_BONDING:
                        // Bonding...
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // Bonded...

                        if(mDevice != null){
                            if(mDevice.getName().contains(BLE_BLOOD_PRESSURE_AD_UA_651BLE)){
                                decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), mDevice, "2");
                            }else if(mDevice.getName().contains(BLE_SCALE_AD_UC_352BLE)){
                                decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), mDevice, "2");
                            }
                            mDevice = null;
                        }
                        break;

                    case BluetoothDevice.BOND_NONE:
                        mDevice = null;
                        // Not bonded...
                        break;
                }
            }
        }
    };


    void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (teleHealthService != null) {
            teleHealthService.unregisterReceiver(teleHealthScanBroadcastReceiver);
        }
        if (teleHealthService != null) {
            teleHealthService.unregisterReceiver(mReceiver);
        }
        teleHealthService = null;
    }
}

