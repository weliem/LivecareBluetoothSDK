package com.example.livecare.bluetoothsdk.initFunctions.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static SharedPreferences sharedPreferences;

    public static void initSharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("com.example.livecare.bluetoothsdk.info", Context.MODE_PRIVATE);
    }

    private static SharedPreferences getAuthCredentials() {
        return sharedPreferences;
    }

    private static SharedPreferences.Editor editSharedPrefs() {
        return getAuthCredentials().edit();
    }

    public static String getStringValue(String key) {
        return sharedPreferences.getString(key, "");
    }

    public static void setStringValue(String key, String value) {
        editSharedPrefs().putString(key, value).commit();
    }

    public static void deleteUserInfo() {
        editSharedPrefs().clear().apply();
    }
}
