package com.example.livecare.bluetoothsdk.initFunctions.data.network;

import com.example.livecare.bluetoothsdk.initFunctions.data.model.AuthTokenModel;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.DataResultModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestRetrofitAPI {
    @POST("sdk/token")
    Call<AuthTokenModel> authenticateUser(@Header("Authorization") String authorization, @Query("grant_type") String grant_type, @Query("androidID") String androidID);

    @POST("sdk/data")
    Call<Object> sendDataResult(@Header("Authorization") String bearerToken, @Body List<DataResultModel> dataResultModels, @Query("androidID") String androidID);
}
