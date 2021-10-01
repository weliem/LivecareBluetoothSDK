package com.example.livecare.bluetoothsdk.initFunctions.data;

import android.content.Context;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataManager {
    private Context mContext;
    private SharedPrefsHelper mSharedPrefsHelper;

    @Inject
    DataManager(Context context,
                SharedPrefsHelper sharedPrefsHelper) {
        mContext = context;
        mSharedPrefsHelper = sharedPrefsHelper;
    }

    public void saveAccessToken(String accessToken) {
        mSharedPrefsHelper.put(SharedPrefsHelper.PREF_KEY_ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken(){
        return mSharedPrefsHelper.get(SharedPrefsHelper.PREF_KEY_ACCESS_TOKEN, null);
    }

    public void setMessage(){
        Toast.makeText(mContext,getAccessToken(),Toast.LENGTH_SHORT).show();
    }
}
