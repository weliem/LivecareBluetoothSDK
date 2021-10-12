package com.example.livecare.bluetoothsdk.initFunctions.utils;

public class Constants {
    public static boolean ViatomScaleConnected;
    public static long currentTimeForLastTelehealthService;
    public static long currentTimeForLastTelehealthServiceBP;
    public static long currentTimeForLastTelehealthServiceGL;
    public static long currentTimeForLastTelehealthServiceScale;
    public static long currentTimeForLastTelehealthServiceSpO2;
    public static long currentTimeForLastTelehealthServiceTEMP;
    public static long currentTimeForLastTelehealthServiceFitnessTracker;
    public static long currentTimeForLastTelehealthServiceECG;
    public static long currentTimeForLastTelehealthServiceTS28B;
    public static long currentTimeForLastTelehealthServiceHS2;
    public static long currentTimeForLastTelehealthServiceHS4S;
    public static long currentTimeForLastTelehealthServiceKN550BT;
    public static long currentTimeForSpirometer;

    //Devices Bluetooth Name
    public static final String BLE_BlOOD_PRESSURE_BP = "Bluetooth BP";
    public static final String BLE_BlOOD_PRESSURE_INDIE_HEALTH = "IH-51-1490-BT";
    public static final String BLE_BlOOD_PRESSURE_ANDES_FIT = "BPM_01";
    public static final String BLE_BlOOD_PRESSURE_TNG_FORA = "TNG BP";
    public static final String BLE_BLOOD_PRESSURE_FORA = "FORA P20";
    public static final String BLE_BLOOD_PRESSURE_TRANSTEK = "1585BS";
    public static final String BLE_BP_WELLUE = "BPM-188";
    public static final String BLE_OMRON_BP1 = "BLESmart_0000015";//"BLESmart_0000015428FFB251DB2C";//BLESmart_
    public static final String BLE_OMRON_BP2 = "BLEsmart_0000015";//"BLEsmart_0000015428FFB251DB2C";//BLEsmart_
    public static final String BLE_OMRON_BP3 = "BLESmart_0000016";
    public static final String BLE_OMRON_BP4 = "BLEsmart_0000016";
    public static final String BLE_PULSE_OXIMETER_BERRYMED = "BerryMed";
    public static final String BLE_PULSE_OXIMETER_FS2OF1 = "Tv221u";
    public static final String BLE_PULSE_OXIMETER_FS2OF2 = "VTM 20F";
    public static final String BLE_PULSE_OXIMETER_ANDES_FIT = "Medical";
    public static final String BLE_SCALE_ANDES_FIT = "SDIC";
    public static final String BLE_SCALE_INDIE_HEALTH = "IndieHealth-W001";
    public static final String BLE_SCALE_INDIE_HEALTH_SMALL = "51-102";
    public static final String BLE_TEMP_ANDES_FIT = "TEMP";
    public static final String BLE_TEMP_AET_WD = "AET-WD";
    public static final String BLE_GLUCOMETER_INDIE_HEALTH = "Oh'Care Lite";
    public static final String BLE_PULSE_OXIMETER_IHEALTH = "Pulse Oximeter";
    public static final String BLE_PULSE_OXIMETER_FORA = "TNG SPO2";
    public static final String BLE_PULSE_OXIMETER_MASIMO = "MightySat";
    public static final String BLE_SCALE_FORA = "TNG SCALE";
    public static final String BLE_SCALE_SMG4 = "SMG4";
    public static final String BLE_SCALE_VIATOM = "Viatom";
    public static final String BLE_GLUCOMETER_PREMIUM_FORA = "FORA PREMIUM V10";
    public static final String BLE_GLUCOMETER_PREMIUM_FORA2 = "TNG";
    public static final String BLE_THERMOMETER_FORA_IR20 = "FORA IR20";
    public static final String BLE_THERMOMETER_VIATOM = "AOJ-20A";
    public static final String BLE_THERMOMETER_UNAAN = "HEAT-LC1";
    public static final String BLE_ECG_IHEALTH = "ECG3";
    public static final String BLE_ECG_VIVALNK = "ECGRec_";
    public static final String BLE_TEMP_VIVALNK = "B4:E7:82";
    public static final String BLE_PRIZMA = "PRZ";
    public static final String BLE_CARDIOBEAT = "WeCardio STD";
    public static final String BLE_GLUCOMETER_CARESENS = "CareSensNPlus";
    public static final String BLE_PULSE_OXIMETER_TAI_DOC = "TAIDOC TD8255";
    public static final String BLE_GLUCOMETER_TAI_DOC = "TAIDOC TD4255";
    public static final String BLE_GLUCOMETER_CONTOUR = "Contour";
    public static final String BLE_BLOOD_PRESSURE_BEURER_BM67 = "BM67";
    public static final String BLE_BLOOD_PRESSURE_BEURER_BC57 = "BC57";
    public static final String BLE_PULSE_OXIMETER_BEURER_PO60 = "PO60";
    public static final String BLE_GLUCOMETER_ACCU_CHECK = "meter+";
    public static final String BLE_BLOOD_PRESSURE_BEURER_BM54 = "BM54";
    public static final String BLE_BLOOD_PRESSURE_CVS = "BP3MW1-4YCVS";
    public static final String BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS = "NiproBGM";
    public static final String BLE_GLUCOMETER_TRUE_METRIX = "TRUEAIR";
    public static final String BLE_GLUCOMETER_AGAMETRIX_CVS = "CVS_HEALTH";
    public static final String BLE_GLUCOMETER_AGAMETRIX_UnPaired = "PQCD";
    public static final String BLE_SCALE_ARBOLEAF = "QN-Scale";
    public static final String BLE_SCALE_QN_SCALE = "ED:67:39";
    public static final String BLE_GLUCOMETER_ONE_TOUCH = "OneTouch";
    public static final String BLE_PULSE_OXIMETER_JUMPER = "My Oximeter";
    public static final String BLE_PULSE_OXIMETER_NONIN = "Nonin3230_";
    public static final String BLE_TEMP_JUMPER = "My Thermometer";
    public static final String BLE_TEMP_JUMPER1 = "Jumper";
    public static final String BLE_BLOOD_PRESSURE_JUMPER = "JPD-HA120";
    public static final String BLE_RING_VIATOM = "O2Ring";
    public static final String BLE_TEMPERATURE_SENSOR_GOVEE = "B5178846C";
    public static final String BLE_V_ALERT = "V.ALRT";
    public static final String BLE_GLUCOMETER_CARESENS_S = "CareSens";
    public static final String BLE_FITNESS_TRACKER = "ID115Plus HR";
    public static final String BLE_SPIROMETER_ANDES_FIT = "BLE-MSA";
    public static final String BLE_SCALE_JUMPER = "JPD Scale";
    public static final String BLE_SPIROMETER_SMART_ONE = "SO-004-";
    public static final String BLE_BLOOD_PRESSURE_AD_UA_651BLE = "A&D_UA-651BLE";
    public static final String BLE_SCALE_AD_UC_352BLE = "A&D_UC-352BLE";


}
