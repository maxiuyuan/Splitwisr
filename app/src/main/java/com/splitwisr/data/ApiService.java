package com.splitwisr.data;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/write")
    Call<Void> writeBalance(@Query("payer") String payer, @Query("payee") String payee, @Query("balance") Double balance);

    @GET("/read")
    Call<JsonArray> readBalance(@Query("current_user") String payer);
}
