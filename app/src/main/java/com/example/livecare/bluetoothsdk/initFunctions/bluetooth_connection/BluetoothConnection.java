package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.AD_BP_UA_651BLE;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.BC57BP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.BM67BP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.BP3MW1;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.BPAndesFit;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.ForaBPP20;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.ForaBPTNG;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.IndieBP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.JumperBP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.LiveCareBP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.TranstekBP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp.WellueBP;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.ecg.ECGCardiBeat;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.emergency_button.VAlertDevice;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.AccuCheckGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.AgaMetrixGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.CareSensGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.CareSense_S_Glucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.ContourGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.ForaGlucometerTNG;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.ForaGlucometerV10;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.IndieGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.OneTouchGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.TaiDocGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer.TrueMetrixAirGlucometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.AD_SCALE_UC_352BLE;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ForaScale;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.IndieScale;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.JumperScale;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ScaleAndesFit;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ScaleArboleaf;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spirometer.SpirometerAndesFit;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.ForaSpO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.JumperSPO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.MasimoSpO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.NoninSpO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.O2RingViatom;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.PO60SPO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.ScanFS2OF_SPO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.ScanSPO2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.ScanSPO2AndesFit;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2.TaiDocSpo2;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.ForaThermometer;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.GoveeH5074TempDevice;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.JumperTemp;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.ThermometerAET;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.ThermometerAndesFit;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.ThermometerUnaan;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.ThermometerViatom;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp.VicksTemp;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.wrist.FitnessTrackerLintelek;
import com.example.livecare.bluetoothsdk.initFunctions.data.local.DBManager;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.AuthTokenModel;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.DataResultModel;
import com.example.livecare.bluetoothsdk.initFunctions.data.network.APIClient;
import com.example.livecare.bluetoothsdk.initFunctions.data.local.PrefManager;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Constants;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleGattCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import androidx.annotation.NonNull;
import java.util.Calendar;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_AD_UA_651BLE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BC57;
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
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_FITNESS_TRACKER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_ACCU_CHECK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_AGAMETRIX_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_AGAMETRIX_UnPaired;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_CARESENS;
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
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SPIROMETER_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMPERATURE_SENSOR_GOVEE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_AET_WD;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_VICKS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_FORA_IR20;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_UNAAN;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_V_ALERT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.auth_refresh_token;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.auth_token;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.token_expires_in;

public class BluetoothConnection {
    private LiveCareMainClass liveCareMainClass;
    private Application app;
    private DBManager dbManager;
    private int pinDevice = 0;
    private int failFlagCount = 0;
    private BluetoothDataResult bluetoothDataResult;
    private BPAndesFit bpAndesFit;
    private SpirometerAndesFit spirometerAndesFit;
    private BM67BP bm67BP;
    private BC57BP bc57BP;
    private PO60SPO2 po60SPO2;
    private CareSensGlucometer careSensGlucometer;
    private AgaMetrixGlucometer agaMetrixGlucometer;
    private TrueMetrixAirGlucometer trueMetrixAirGlucometer;
    private IndieBP indieBP;
    private IndieGlucometer indieGlucometer;
    private IndieScale indieScale;
    private JumperScale jumperScale;
    private OneTouchGlucometer oneTouchGlucometer;
    private AccuCheckGlucometer accuCheckGlucometer;
    private AD_BP_UA_651BLE ad_bp_ua_651BLE;
    private AD_SCALE_UC_352BLE ad_scale_uc_352BLE;
    private ECGCardiBeat ecgCardiBeat;
    private TranstekBP transtekBP;
    /* private OmronBP omronBP;
    private VivaLNKTemperature vivaLNKTemperature;
    private VivaLNKECGBackground vivaLNKECGBackground; */

    public BluetoothConnection(LiveCareMainClass liveCareMainClass, BluetoothDataResult bluetoothDataResult, Application app) {
        this.app = app;
        this.liveCareMainClass = liveCareMainClass;
        this.bluetoothDataResult = bluetoothDataResult;
        dbManager = new DBManager(app);
        dbManager.open();
    }

    public void addDeviceFromScanning(BleDevice bleDevice, String devicesOrigin, String deviceName) {
        Utils.teleHealthScanBroadcastReceiver(false);
        if (!BleManager.getInstance().getIsConnecting()){
            connect(deviceName, bleDevice);
        }
    }

    private void connect(String deviceName, final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                bluetoothDataResult.onStartConnect(deviceName);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                if (BLE_BlOOD_PRESSURE_BP.equalsIgnoreCase(bleDevice.getName())) {
                    if (exception.getDescription().equalsIgnoreCase("Timeout Exception Occurred!")) {
                        connect(deviceName, bleDevice);
                        failFlagCount++;
                    } else {
                        failFlagCount = 0;
                    }

                    if (failFlagCount > 2) {
                        connectionFailed(deviceName,exception.getDescription());
                    }

                    if (exception.getDescription().contains("Gatt Exception Occurred")) {
                        connectionFailed(deviceName, exception.getDescription());
                    }
                } else {
                    connectionFailed(deviceName, exception.getDescription());
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bluetoothDataResult.OnConnectedSuccess(deviceName);
                if (gatt != null) {
                    onConnectedSuccess(bleDevice, gatt);
                } else {
                    BleManager.getInstance().disconnect(bleDevice);
                }
                startScanAgain(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status, int disconnectFlag) {
                bluetoothDataResult.onDisConnected(deviceName);
                sendDataOnDisconnect(bleDevice);
                Utils.setTimeOnDisconnect(bleDevice.getName());
                Constants.currentTimeForLastTelehealthService = Calendar.getInstance().getTime().getTime();
            }
        });
    }

    private void startScanAgain(BleDevice bleDevice) {
        if (bleDevice.getName() != null) {
            if (!(bleDevice.getName().contains(BLE_GLUCOMETER_CARESENS)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_AGAMETRIX_CVS)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_AGAMETRIX_UnPaired)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_TRUE_METRIX)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_ONE_TOUCH)
                    || bleDevice.getName().contains(BLE_GLUCOMETER_ACCU_CHECK)
                    || bleDevice.getName().contains(BLE_OMRON_BP1)
                    || bleDevice.getName().contains(BLE_OMRON_BP2)
                    || bleDevice.getName().contains(BLE_OMRON_BP3)
                    || bleDevice.getName().contains(BLE_OMRON_BP4)
                    || bleDevice.getName().contains(BLE_PRIZMA)
                    || bleDevice.getName().contains(BLE_PULSE_OXIMETER_BEURER_PO60))) {
                Utils.teleHealthScanBroadcastReceiver(true);
            }
        }
    }

    private void connectionFailed(String deviceName, String message){
        bluetoothDataResult.OnConnectFail(deviceName, message);
        Utils.teleHealthScanBroadcastReceiver(true);
    }

    private void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        if (device.getName() != null) {
            switch (device.getName()) {
                case BLE_PULSE_OXIMETER_BERRYMED:
                    new ScanSPO2(this,device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_FS2OF1:
                case BLE_PULSE_OXIMETER_FS2OF2:
                    ScanFS2OF_SPO2 scanFS2OF_spo2 = new ScanFS2OF_SPO2(this);
                    scanFS2OF_spo2.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BlOOD_PRESSURE_ANDES_FIT:
                    bpAndesFit = new BPAndesFit(this);
                    bpAndesFit.onConnectedSuccess(device, gatt);
                    break;

                case BLE_SCALE_ANDES_FIT:
                    ScaleAndesFit scaleAndesFit = new ScaleAndesFit(this);
                    scaleAndesFit.onConnectedSuccess(device, gatt);
                    break;

                case BLE_TEMP_ANDES_FIT:
                    ThermometerAndesFit thermometerAndesFit = new ThermometerAndesFit(this);
                    thermometerAndesFit.onConnectedSuccess(device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_ANDES_FIT:
                    ScanSPO2AndesFit scanSPO2AndesFit = new ScanSPO2AndesFit(this);
                    scanSPO2AndesFit.onConnectedSuccess(device, gatt);
                    break;

                case BLE_SPIROMETER_ANDES_FIT:
                    spirometerAndesFit = new SpirometerAndesFit(this);
                    spirometerAndesFit.onConnectedSuccess(device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_BEURER_PO60:
                    po60SPO2 = new PO60SPO2(this);
                    po60SPO2.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BLOOD_PRESSURE_BEURER_BM67:
                    bm67BP = new BM67BP(this);
                    bm67BP.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BLOOD_PRESSURE_BEURER_BC57:
                    bc57BP = new BC57BP(this);
                    bc57BP.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BLOOD_PRESSURE_CVS:
                    BP3MW1 bp3MW1 = new BP3MW1(this);
                    bp3MW1.onConnectedSuccess(device, gatt);
                    break;

                case BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS:
                case BLE_GLUCOMETER_TRUE_METRIX:
                    trueMetrixAirGlucometer = new TrueMetrixAirGlucometer(this);
                    trueMetrixAirGlucometer.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BLOOD_PRESSURE_FORA:
                    ForaBPP20 foraBPP20 = new ForaBPP20(this);
                    foraBPP20.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BlOOD_PRESSURE_TNG_FORA:
                    ForaBPTNG foraBPTNG = new ForaBPTNG(this);
                    foraBPTNG.onConnectedSuccess(device, gatt);
                    break;

                case BLE_GLUCOMETER_PREMIUM_FORA2:
                    ForaGlucometerTNG foraGlucometerTNG = new ForaGlucometerTNG(this);
                    foraGlucometerTNG.onConnectedSuccess(device, gatt);
                    break;

                case BLE_GLUCOMETER_PREMIUM_FORA:
                    ForaGlucometerV10 foraGlucometerV10 = new ForaGlucometerV10(this);
                    foraGlucometerV10.onConnectedSuccess(device, gatt);
                    break;

                case BLE_SCALE_FORA:
                    ForaScale foraScale = new ForaScale(this);
                    foraScale.onConnectedSuccess(device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_FORA:
                    ForaSpO2 foraSpO2 = new ForaSpO2(this);
                    foraSpO2.onConnectedSuccess(device, gatt);
                    break;

                case BLE_THERMOMETER_FORA_IR20:
                    ForaThermometer foraThermometer = new ForaThermometer(this);
                    foraThermometer.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BlOOD_PRESSURE_INDIE_HEALTH:
                    indieBP = new IndieBP(this);
                    indieBP.onConnectedSuccess(device, gatt);
                    break;

                case BLE_GLUCOMETER_INDIE_HEALTH:
                    indieGlucometer = new IndieGlucometer(this);
                    indieGlucometer.onConnectedSuccess(device, gatt);
                    break;

                case BLE_SCALE_INDIE_HEALTH:
                    indieScale = new IndieScale(this);
                    indieScale.onConnectedSuccess(device, gatt, 1);
                    break;
                case BLE_SCALE_INDIE_HEALTH_SMALL:
                    indieScale = new IndieScale(this);
                    indieScale.onConnectedSuccess(device, gatt, 2);
                    break;

                case BLE_BLOOD_PRESSURE_JUMPER:
                    JumperBP jumperBP = new JumperBP(this);
                    jumperBP.onConnectedSuccess(device, gatt);
                    break;

                case BLE_SCALE_JUMPER:
                    jumperScale = new JumperScale(this);
                    jumperScale.onConnectedSuccess(device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_JUMPER:
                    JumperSPO2 jumperSPO2 = new JumperSPO2(this);
                    jumperSPO2.onConnectedSuccess(device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_MASIMO:
                    new MasimoSpO2(this, device, gatt);
                    break;

                case BLE_CARDIOBEAT:
                    ecgCardiBeat = new ECGCardiBeat(this);
                    ecgCardiBeat.onConnectedSuccess(device, gatt);
                    break;

                case BLE_BlOOD_PRESSURE_BP:
                    new LiveCareBP(this, device, gatt);
                    break;

                case BLE_SCALE_ARBOLEAF:
                    new ScaleArboleaf(this,device, gatt);
                    break;

                case BLE_TEMP_AET_WD:
                    new ThermometerAET(this,device, gatt);
                    break;

                case BLE_THERMOMETER_UNAAN:
                    new ThermometerUnaan(this,device, gatt);
                    break;
                case BLE_BLOOD_PRESSURE_TRANSTEK:
                    transtekBP = new TranstekBP(this,device, gatt);
                    break;

                case BLE_TEMP_VICKS:
                    new VicksTemp(this, device, gatt);
                    break;

                case BLE_BP_WELLUE:
                    new WellueBP(this, device, gatt);
                    break;

                case BLE_GLUCOMETER_TAI_DOC:
                    new TaiDocGlucometer(this, device, gatt);
                    break;

                case BLE_PULSE_OXIMETER_TAI_DOC:
                    new TaiDocSpo2(this, device, gatt);
                    break;

                case BLE_V_ALERT:
                    new VAlertDevice(this, device, gatt);
                    break;

                case BLE_THERMOMETER_VIATOM:
                    new ThermometerViatom(this, device, gatt);
                    break;

                default:
                    if (device.getName().contains(BLE_OMRON_BP1) || device.getName().contains(BLE_OMRON_BP2) ||
                            device.getName().contains(BLE_OMRON_BP3) || device.getName().contains(BLE_OMRON_BP4)) {

                        //omronBP = new OmronBP(bluetoothConnectionFragment, mContext, deviceName);
                       // omronBP.onConnectedSuccess(device, gatt);
                    } else if(device.getName().startsWith(BLE_GLUCOMETER_CARESENS_S)){
                        if(device.getName().contains(BLE_GLUCOMETER_CARESENS)){
                            careSensGlucometer = new CareSensGlucometer(this);
                            careSensGlucometer.onConnectedSuccess(device, gatt);
                        }else {
                            CareSense_S_Glucometer careSense_s_glucometer = new CareSense_S_Glucometer(this);
                            careSense_s_glucometer.onConnectedSuccess(device, gatt);
                        }
                    } else if (device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_CVS) || device.getName().contains(BLE_GLUCOMETER_AGAMETRIX_UnPaired)) {
                        agaMetrixGlucometer = new AgaMetrixGlucometer(this);
                        agaMetrixGlucometer.onConnectedSuccess(device, gatt);
                    }else if (device.getName().contains(BLE_TEMPERATURE_SENSOR_GOVEE)) {
                        GoveeH5074TempDevice goveeH5074TempDevice = new GoveeH5074TempDevice(this);
                        goveeH5074TempDevice.onConnectedSuccess(device, gatt);
                    }  else if (device.getName().contains(BLE_TEMP_JUMPER) || device.getName().contains(BLE_TEMP_JUMPER1)) {
                        JumperTemp jumperTemp = new JumperTemp(this);
                        jumperTemp.onConnectedSuccess(device, gatt);
                    }else if (device.getName().startsWith(BLE_PULSE_OXIMETER_NONIN)) {
                         new NoninSpO2(this,device, gatt);
                    }  else if (device.getName().contains(BLE_GLUCOMETER_ONE_TOUCH)) {
                        oneTouchGlucometer = new OneTouchGlucometer(this, device, gatt);
                    }else if (device.getName().contains(BLE_GLUCOMETER_ACCU_CHECK)) {
                        accuCheckGlucometer = new AccuCheckGlucometer(this);
                        accuCheckGlucometer.onConnectedSuccess(device,gatt);
                    } else if (device.getName().startsWith(BLE_BLOOD_PRESSURE_AD_UA_651BLE)) {
                        ad_bp_ua_651BLE = new AD_BP_UA_651BLE(this);
                        ad_bp_ua_651BLE.onConnectedSuccess(device, gatt);
                    } else if (device.getName().startsWith(BLE_SCALE_AD_UC_352BLE)) {
                        ad_scale_uc_352BLE = new AD_SCALE_UC_352BLE(this);
                        ad_scale_uc_352BLE.onConnectedSuccess(device, gatt);
                    } else if (device.getName().contains(BLE_GLUCOMETER_CONTOUR)) {
                        ContourGlucometer contourGlucometer = new ContourGlucometer(this);
                        contourGlucometer.onConnectedSuccess(device, gatt);
                    }else if (device.getName().contains(BLE_RING_VIATOM)) {
                        new O2RingViatom(this, device, gatt);
                    }else if (device.getName().contains(BLE_FITNESS_TRACKER)) {
                        new FitnessTrackerLintelek(this, device, gatt);
                    }

                    /*else if (device.getName().contains(BLE_PRIZMA)) {
                        new PrizmaDevice(this).onConnectedSuccess( device);
                    }*/
            }
        }
    }

    private void sendDataOnDisconnect(BleDevice bleDevice) {
        if (bleDevice.getName() == null) {
            return;
        }
        if (bleDevice.getName().equalsIgnoreCase(BLE_BLOOD_PRESSURE_BEURER_BM67) && bm67BP != null) {
            bm67BP.onDisConnected();
        }

        if (bleDevice.getName().equalsIgnoreCase(BLE_BLOOD_PRESSURE_BEURER_BC57) && bc57BP != null) {
            bc57BP.onDisConnected();
        }

        if (bleDevice.getName().equalsIgnoreCase(BLE_BlOOD_PRESSURE_INDIE_HEALTH) && indieBP != null) {
            indieBP.onDisConnected();
        }

        if (bleDevice.getName().equalsIgnoreCase(BLE_CARDIOBEAT) && ecgCardiBeat != null) {
            ecgCardiBeat.onDisConnected();
        }

        if (bleDevice.getName().equalsIgnoreCase(BLE_BLOOD_PRESSURE_TRANSTEK) && transtekBP != null) {
            transtekBP.onDisConnected();
        }

        /*if (bleDevice.getName().startsWith(BLE_PRIZMA)) {
              bluetoothConnectionFragment.forceFinishActivity();
        }*/
    }

    private long timeOnDataReceived = 0;
    public void onDataReceived(Map<String, Object> data, String deviceType, String mac, String name) {
        if(liveCareMainClass != null){
            if(timeOnDataReceived != 0){
                if(Calendar.getInstance().getTime().getTime() - timeOnDataReceived > 5000){
                    timeOnDataReceived = Calendar.getInstance().getTime().getTime();
                    bluetoothDataResult.onDataReceived(data, deviceType);
                    sendDataResult(data,deviceType,mac,name,Calendar.getInstance().getTime().getTime());
                }
            }else {
                timeOnDataReceived = Calendar.getInstance().getTime().getTime();
                bluetoothDataResult.onDataReceived(data, deviceType);
                sendDataResult(data,deviceType,mac,name,Calendar.getInstance().getTime().getTime());
            }
        }
    }

    private void sendDataResult(Map<String, Object> data, String deviceType, String mac, String deviceName, long createdAt){
        DataResultModel dataResultModel = new DataResultModel(data, deviceType, mac, deviceName, createdAt);
        dbManager.insert(dataResultModel);
        String androidID = Settings.Secure.getString(app.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(Calendar.getInstance().getTime().getTime() < PrefManager.getLongValue(token_expires_in)){

            Call<Object> call = APIClient.getData().sendDataResult("Bearer " +PrefManager.getStringValue(auth_token), dbManager.dataResultModel(),
                    androidID);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                    if(response.isSuccessful()){
                        dbManager.delete();
                    }else {
                        authenticateUser(data,deviceType,mac,deviceName,Calendar.getInstance().getTime().getTime(),androidID);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {

                }
            });
        }else {
            authenticateUser(data,deviceType,mac,deviceName,Calendar.getInstance().getTime().getTime(),androidID);
        }

    }

    private void authenticateUser(Map<String, Object> data, String deviceType, String mac, String deviceName, long time, String androidID){
        String authHeader = "Bearer " + PrefManager.getStringValue(auth_refresh_token);
        Call<AuthTokenModel> call = APIClient.getData().authenticateUser(authHeader,"refresh_token", androidID);
        call.enqueue(new Callback<AuthTokenModel>() {
            @Override
            public void onResponse(@NonNull Call<AuthTokenModel> call, @NonNull Response<AuthTokenModel> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    PrefManager.setStringValue(auth_token, response.body().getToken());
                    PrefManager.setStringValue(auth_refresh_token,response.body().getRefresh_token());
                    Long expirationTime = Calendar.getInstance().getTime().getTime() + response.body().getExpires_in()+5;
                    PrefManager.setLongValue(token_expires_in,expirationTime);
                    sendDataResult(data,deviceType,mac,deviceName,time);
                    Utils.startTeleHealthService();
                }else {
                    PrefManager.setStringValue(auth_token, "");
                    PrefManager.setStringValue(auth_refresh_token, "");
                    PrefManager.setLongValue(token_expires_in,0L);
                    Utils.stopTeleHealthService();
                    bluetoothDataResult.authenticationStatus("on Error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthTokenModel> call, @NonNull Throwable t) {
                bluetoothDataResult.authenticationStatus(t.getMessage());
            }
        });
    }


    public void setPin(BluetoothDevice device, int deviceFlag) {
        pinDevice = deviceFlag;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        app.registerReceiver(mReceiver, filter);
        Utils.pair(device);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch(state){
                    case BluetoothDevice.BOND_BONDING:
                        // Bonding...
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // Bonded...
                        app.unregisterReceiver(mReceiver);
                        connectAfterPinSet(pinDevice);
                        break;

                    case BluetoothDevice.BOND_NONE:
                        // Not bonded...
                        break;
                }
            }
        }
    };

    private void connectAfterPinSet(int deviceFlag) {
        if (deviceFlag == 1) {
            careSensGlucometer.startNotifyCustom();
        } else if (deviceFlag == 2) {
            po60SPO2.startNotifyCustom();
        } else if (deviceFlag == 3) {
            accuCheckGlucometer.startNotifyCustom();
        } else if (deviceFlag == 5) {
             trueMetrixAirGlucometer.startNotifyCustom();
        } else if (deviceFlag == 6) {
            agaMetrixGlucometer.startNotifyCustom();
        } else if (deviceFlag == 7) {
            oneTouchGlucometer.startNotifyCustom();
        }
    }

    public void onDestroy() {
        dbManager.close();
        liveCareMainClass = null;

        if (bpAndesFit != null) {
            bpAndesFit.onDestroy();
        }

        if (spirometerAndesFit != null) {
            spirometerAndesFit.onDestroy();
        }

        if (po60SPO2 != null) {
            po60SPO2.onDestroy();
        }

        if (indieGlucometer != null) {
            indieGlucometer.onDestroy();
        }

        if (indieScale != null) {
            indieScale.onDestroy();
        }

        if (jumperScale != null) {
            jumperScale.onDestroy();
        }

        if (ad_bp_ua_651BLE != null) {
            ad_bp_ua_651BLE.onDestroy();
        }

        if (ad_scale_uc_352BLE != null) {
            ad_scale_uc_352BLE.onDestroy();
        }

        /*if (omronBP != null) {
            omronBP.onDestroy();
        }

        if (vivaLNKTemperature != null) {
            vivaLNKTemperature.onDestroy();
        }

        if (vivaLNKECGBackground != null) {
            vivaLNKECGBackground.onDestroy();
        }*/

    }
}
