package com.example.cafemanagement.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {
    // Endpoint của ZaloPay Sandbox
    @POST("v2/create")
    Call<Map<String, Object>> createZaloPayOrder(@Body Map<String, Object> body);

    // Endpoint của MoMo Sandbox
    @POST("v2/gateway/api/create")
    Call<Map<String, Object>> createMoMoOrder(@Body Map<String, Object> body);
}