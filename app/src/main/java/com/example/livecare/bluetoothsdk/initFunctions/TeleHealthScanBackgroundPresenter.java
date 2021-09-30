package com.example.livecare.bluetoothsdk.initFunctions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.enums.BleDevicesName;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Constants;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleScanCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.Calendar;
import java.util.List;

import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_ANDES_FIT;

public class TeleHealthScanBackgroundPresenter {

    private static final String TAG = "TeleHealthScan";
    private TeleHealthService teleHealthService;
    private long startTime = 0;
    private Handler handler;
    private boolean startScan;
    //private VAlertDevice vAlertDevice;
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

    public TeleHealthScanBackgroundPresenter(TeleHealthService teleHealthService) {
        this.teleHealthService = teleHealthService;
        startScan = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("teleHealthScan.BroadcastReceiver");
        teleHealthService.registerReceiver(teleHealthScanBroadcastReceiver, filter);
    }

    public void iniFunction() {
        startTime = System.currentTimeMillis();
        handler = new Handler();

        if (!"Link+".equalsIgnoreCase(BleManager.getInstance().getBluetoothAdapter().getName())) {
            BleManager.getInstance().getBluetoothAdapter().setName("Link+");
        }

        startScan();
        if (teleHealthService != null) {
            notifyScanStarted(teleHealthService);
        }
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
                    }
                }

                @Override
                public void onLeScan(BleDevice bleDevice) {
                    super.onLeScan(bleDevice);
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    Log.d(TAG, "onScanning: " + bleDevice.getName() + " mac " + bleDevice.getMac() + " start time " + startTime);
                    scanDevicesResponse(bleDevice);
                }

                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
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

    private boolean isAnyDeviceConnected() {
        if (!Utils.isAnyBleDeviceConnected()) {
           // if (BleManagerViVaTemp.mInstance == null) {
              //  if (VitalClient.getInstance() == null || !VitalClient.getInstance().isInitial) {
                    return Utils.isAnyBleDeviceConnecting();
               // }
           // }
        }else {
            //if(Utils.isNotForegroundBleDeviceConnected()){
                if(teleHealthService!=null){
                    teleHealthService.checkIfBackGroundIHealthIsConnected();
                }
           // }
        }
        return true;
    }

    private void scanDevicesResponse(BleDevice device) {
        String iHealthDevices = "1";//all iHealth devices will go with this flag to ScanActivity
        String otherDevices = "2";//all other devices will go with this flag to ScanActivity
        String sdkDevices = "3";//VivaLNK devices will go with this flag to ScanActivity
        //String fitnessDevices = "4";//fitnessDevices devices will go with this flag to ScanActivity

        if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthService < 5000) {
            return;
        }
        if (device.getName() != null) {
            Log.d(TAG, "scanDevicesResponse: "+ device.getName());
            switch (device.getName()) {
                //case BLE_BlOOD_PRESSURE_BP:
                case BLE_BlOOD_PRESSURE_ANDES_FIT:
               // case BLE_BLOOD_PRESSURE_TRANSTEK:
               // case BLE_BLOOD_PRESSURE_JUMPER:
                    if ((Calendar.getInstance().getTime().getTime() - Constants.currentTimeForLastTelehealthServiceBP > 20000)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.BP.stringValue, device, otherDevices, TypeBleDevices.BP.stringValue,false);
                    }
                    break;

               /* case BLE_BlOOD_PRESSURE_INDIE_HEALTH:
                case BLE_BLOOD_PRESSURE_FORA:
                case BLE_BlOOD_PRESSURE_TNG_FORA:
                case BLE_BP_WELLUE:
                case BLE_BLOOD_PRESSURE_BEURER_BM67:
                case BLE_BLOOD_PRESSURE_BEURER_BC57:
                case BLE_BLOOD_PRESSURE_BEURER_BM54:
                case BLE_BLOOD_PRESSURE_CVS:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices, TypeBleDevices.BP.stringValue,false);
                    break;

                case BLE_GLUCOMETER_PREMIUM_FORA:
                case BLE_GLUCOMETER_INDIE_HEALTH:
                case BLE_GLUCOMETER_PREMIUM_FORA2:
                case BLE_GLUCOMETER_TAI_DOC:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                    break;

                case BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS:
                case BLE_GLUCOMETER_TRUE_METRIX:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceGL > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                    }
                    break;

                case BLE_PULSE_OXIMETER_BERRYMED:
                case BLE_PULSE_OXIMETER_ANDES_FIT:
                case BLE_PULSE_OXIMETER_FS2OF1:
                case BLE_PULSE_OXIMETER_FS2OF2:
                case BLE_PULSE_OXIMETER_FORA:
                case BLE_PULSE_OXIMETER_MASIMO:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceSpO2 > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices, TypeBleDevices.SpO2.stringValue,false);
                    }
                    break;

                case BLE_PULSE_OXIMETER_TAI_DOC:
                case BLE_PULSE_OXIMETER_BEURER_PO60:
                case BLE_PULSE_OXIMETER_JUMPER:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices, TypeBleDevices.SpO2.stringValue,false);
                    break;

                case BLE_PULSE_OXIMETER_IHEALTH:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, iHealthDevices, TypeBleDevices.SpO2.stringValue, PrefManager.isBooleanSet(enable_background_connection));
                    break;

                case BLE_SCALE_ANDES_FIT:
                case BLE_SCALE_INDIE_HEALTH_SMALL:
                case BLE_SCALE_JUMPER:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceScale > 20000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, otherDevices, TypeBleDevices.WS.stringValue,false);
                    }
                    break;

                case BLE_SCALE_INDIE_HEALTH:
                case BLE_SCALE_FORA:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, otherDevices, TypeBleDevices.WS.stringValue,false);
                    break;

                case BLE_SCALE_SMG4:
                case BLE_SCALE_VIATOM:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceScale > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices, TypeBleDevices.WS.stringValue,false);
                    }
                    break;

                case BLE_TEMP_ANDES_FIT:
                    if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 70000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                    }
                    break;

                case BLE_TEMP_AET_WD:
                case BLE_THERMOMETER_FORA_IR20:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                    break;

                case BLE_THERMOMETER_VIATOM:
                    if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 80000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                    }
                    break;

                case BLE_THERMOMETER_UNAAN:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 30000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                    }
                    break;

                case BLE_ECG_IHEALTH:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, iHealthDevices, TypeBleDevices.ECG.stringValue,false);
                    break;

                case BLE_CARDIOBEAT:
                    decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, otherDevices, TypeBleDevices.ECG.stringValue,false);
                    break;

                case BLE_SCALE_ARBOLEAF:
                    if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthService > 10000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices, TypeBleDevices.WS.stringValue,false);
                    }
                    break;

                case iHealthDevicesManager.TYPE_PT3SBT:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForPT3SBT > 600000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, iHealthDevices, TypeBleDevices.Temp.stringValue,false);
                    }
                    break;

                case BLE_SPIROMETER_ANDES_FIT:
                    if (Calendar.getInstance().getTime().getTime() - Constants.currentTimeForSpirometer > 100000) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.SpiroMeter.toString(), device, otherDevices, TypeBleDevices.Spirometer.stringValue,false);
                    }
                    break;

                    default:
                    if (device.getName().contains("iHealth HS4S")) {
                        if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceHS4S > 60000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue,false);
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_TS28B)) {
                        if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTS28B > 40000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, iHealthDevices, TypeBleDevices.Temp.stringValue,false);
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_BG5S)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, iHealthDevices, TypeBleDevices.Gl.stringValue, PrefManager.isBooleanSet(enable_background_connection));
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_HS2)) {
                        if(device.getName().startsWith(iHealthDevicesManager.TYPE_HS2S)){
                            decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue, PrefManager.isBooleanSet(enable_background_connection));
                        }else {
                            if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceHS2 > 45000) {
                                decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, iHealthDevices, TypeBleDevices.WS.stringValue,false);
                            }
                        }
                    } else if (device.getName().contains(iHealthDevicesManager.TYPE_550BT)) {
                        if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceKN550BT > 15000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, iHealthDevices, TypeBleDevices.BP.stringValue, false);
                        }
                    } else if (device.getName().contains(BLE_OMRON_BP1) || device.getName().contains(BLE_OMRON_BP2) ||
                            device.getName().contains(BLE_OMRON_BP3) || device.getName().contains(BLE_OMRON_BP4)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices, TypeBleDevices.BP.stringValue,false);

                    } else if (device.getName().startsWith(BLE_GLUCOMETER_CARESENS_S)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                    } else if (device.getName().contains(BLE_GLUCOMETER_CONTOUR)) {
                        if (checkPairedDevices(device.getMac())) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                        } else {
                            try {
                                if (createBond(device.getDevice())) {
                                    handler.postDelayed(() -> decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false), 2000);
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    } else if (device.getName().contains(BLE_GLUCOMETER_ACCU_CHECK)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                    } else if (device.getName().contains(BLE_ECG_VIVALNK)) {
                        if (!"".equals(PrefManager.getStringValue(BLE_ECG_VIVALNK))) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.ECG.toString(), device, sdkDevices, TypeBleDevices.ECG.stringValue,false);
                        }
                    } else if (device.getName().contains(BLE_PRIZMA)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Prizma.toString(), device, otherDevices, TypeBleDevices.PRIZMA.stringValue,false);
                    } else if (device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_CVS) || device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_UnPaired)) {
                        decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                    } else if (device.getName().contains(BLE_GLUCOMETER_ONE_TOUCH)) {
                        if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceGL > 120000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Gl.toString(), device, otherDevices, TypeBleDevices.Gl.stringValue,false);
                        }
                    } else if (device.getName().contains(BLE_TEMP_JUMPER) || device.getName().contains(BLE_TEMP_JUMPER1)) {
                        if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceTEMP > 70000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                        }
                    } else if (device.getName().contains(BLE_TEMPERATURE_SENSOR_GOVEE)) {
                        if (PrefManager.isBooleanSet(Govee_Key)) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue, false);
                        }
                    } else if (device.getName().contains(BLE_TEMPERATURE_SENSOR_GOVEE)) {
                        if (!"".equals(PrefManager.getStringValue(Govee_Key))) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, otherDevices, TypeBleDevices.Temp.stringValue,false);
                        }
                    } else if (device.getName().contains(BLE_V_ALERT)) {
                        Timber.tag(TAG).d("BLE_V_ALERT is connected: %s", BleManager.getInstance().isConnected(device));
                        if (!BleManager.getInstance().isConnected(device) && teleHealthService != null) {
                            if (!iHealthScanActivity && !PrefManager.isBooleanSet(V_Alert_Key)) {
                                BleManager.getInstance().cancelScan();
                                startScan = false;
                                vAlertDevice = new VAlertDevice(teleHealthService, this);
                                vAlertDevice.connect(device);
                            }
                        }
                    } else if (device.getName().contains(BLE_SPIROMETER_SMART_ONE)) {
                        teleHealthService.broadcastBluetoothFragment("display_spirometer","true");
                    } else if (device.getName().startsWith(BLE_PULSE_OXIMETER_NONIN)) {
                        if (!BleManager.getInstance().isConnected(device) && BleManager.getInstance().getConnectState(device) == 0 && Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthServiceSpO2 > 30000) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices, TypeBleDevices.SpO2.stringValue,false);
                        }
                    } else if (device.getName().contains(BLE_BLOOD_PRESSURE_AD_UA_651BLE)) {
                        if (checkPairedDevices(device.getMac())) {
                            unPairDevice(device.getDevice());
                        } else {
                            try {
//                                if (createBond(device.getDevice())) {
//                                    decisionFunctionAfterGettingBTMac(BleDevicesName.BP.toString(), device, otherDevices, TypeBleDevices.BP.stringValue, false);
//                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    } else if (device.getName() != null && device.getName().contains(BLE_RING_VIATOM)) {
                        if (!"".equals(PrefManager.getStringValue(BLE_RING_VIATOM))) {
                            decisionFunctionAfterGettingBTMac(BleDevicesName.SpO2.toString(), device, otherDevices, TypeBleDevices.SpO2.stringValue,false);
                        }
                    }*/
            }
        } else {
           /* if (device.getMac().contains(BLE_TEMP_VIVALNK)) {
                if (!"".equals(PrefManager.getStringValue(BLE_TEMP_VIVALNK)) && BleManagerViVaTemp.mInstance == null) {
                    decisionFunctionAfterGettingBTMac(BleDevicesName.Temp.toString(), device, sdkDevices, TypeBleDevices.Temp.stringValue,false);
                }
            } else if (device.getMac().contains(BLE_SCALE_QN_SCALE)) {
                if (Calendar.getInstance().getTime().getTime() - currentTimeForLastTelehealthService > 10000) {
                    decisionFunctionAfterGettingBTMac(BleDevicesName.WS.toString(), device, sdkDevices, TypeBleDevices.WS.stringValue,false);
                }
            }*/
        }
    }

    public void watchConnected() {
        startScan = true;
        startScan();
        broadcastMainBaseActivity();
        //Constants.DISPLAY_WATCH_ICON = true;
    }

    public void watchDisConnected() {
        //Constants.DISPLAY_WATCH_ICON = false;
        broadcastMainBaseActivity();
    }

    public void resumeScan() {
        startScan = true;
        startScan();
    }

    private void broadcastMainBaseActivity() {
        if (teleHealthService != null) {
            Intent local = new Intent();
            local.setAction("broadcast.watchReceiver");
            local.putExtra("main_base_activity", "show_watch");
            teleHealthService.sendBroadcast(local);
        }
    }

    private void decisionFunctionAfterGettingBTMac(String deviceName, BleDevice device, String iHealthOrOtherDevice, String type, boolean isBackground) {
        if (teleHealthService != null) {
            teleHealthService.setDeviceFound(deviceName, device, iHealthOrOtherDevice, isBackground);
        }
    }


    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (teleHealthService != null) {
            teleHealthService.unregisterReceiver(teleHealthScanBroadcastReceiver);
        }
        teleHealthService = null;
    }
}

