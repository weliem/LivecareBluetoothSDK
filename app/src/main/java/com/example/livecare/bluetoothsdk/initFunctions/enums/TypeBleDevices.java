package com.example.livecare.bluetoothsdk.initFunctions.enums;

public enum TypeBleDevices {
    BP("BP"),
    Gl("Gl"),
    SpO2("SpO2"),
    Temp("Temp"),
    WS("WS"),
    ECG("ECG"),
    PRIZMA("Prizma"),
    V_ALERT("V_ALERT"),
    Fitness("Fitness"),
    Spirometer("Spirometer");

    public final String stringValue;

    TypeBleDevices(String s) {
        this.stringValue = s;
    }
}
