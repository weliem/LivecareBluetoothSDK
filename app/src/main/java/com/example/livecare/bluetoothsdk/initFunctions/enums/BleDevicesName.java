package com.example.livecare.bluetoothsdk.initFunctions.enums;

public enum BleDevicesName {
    BP("BP Monitor"),
/*    Gl("Gl"),
    SpO2("Pulse Oximeter"),
    Temp("Temp"),
    WS("WS"),
    ECG("ECG"),
    PRIZMA("Prizma"),
    V_ALERT("V_ALERT"),
    Fitness("Fitness"),*/
    Spirometer("Spirometer");

    public final String stringValue;

    BleDevicesName(String s) {
        this.stringValue = s;
    }
}
