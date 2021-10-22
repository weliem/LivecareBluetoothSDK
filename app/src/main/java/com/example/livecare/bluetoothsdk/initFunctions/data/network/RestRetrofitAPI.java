package com.example.livecare.bluetoothsdk.initFunctions.data.network;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestRetrofitAPI {
    @POST("patients/subscriptionActivate")
    Call<Object> authenticateUser(@Header("Authorization") String bearerToken, @Query("code") String code);

    @POST("patients/subscriptionActivate")
    Call<Object> sendDataResult(@Header("Authorization") String bearerToken, @Query("code") String code);
}
