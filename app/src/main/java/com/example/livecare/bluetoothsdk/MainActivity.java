package com.example.livecare.bluetoothsdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LiveCareMainClass.getInstance().init(getApplication());
    }
}
