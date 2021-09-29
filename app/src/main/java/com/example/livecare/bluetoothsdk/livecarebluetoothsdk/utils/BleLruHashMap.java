package com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils;

import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.bluetooth.BleBluetooth;
import java.util.LinkedHashMap;

public class BleLruHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int MAX_SIZE;

    public BleLruHashMap(int saveSize) {
        super((int) Math.ceil(saveSize / 0.75) + 1, 0.75f, true);
        MAX_SIZE = saveSize;
    }

    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        if (size() > MAX_SIZE && eldest.getValue() instanceof BleBluetooth) {
            ((BleBluetooth) eldest.getValue()).disconnect(0);
        }
        return size() > MAX_SIZE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<K, V> entry : entrySet()) {
            sb.append(String.format("%s:%s ", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

}
