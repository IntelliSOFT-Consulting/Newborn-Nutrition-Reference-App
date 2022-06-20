package com.intellisoft.nndak.api

import com.intellisoft.nndak.charts.*
import com.intellisoft.nndak.data.AuthResponse
import com.intellisoft.nndak.data.LoginData
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.data.UserResponse
import retrofit2.Call
import retrofit2.http.*


interface AuthService {

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun loginUser(@Body data: LoginData): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/reset-password")
    fun resetPassword(@Body data: LoginData): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @GET("auth/me")
    fun loadUser(): Call<UserResponse>


    @Headers("Content-Type: application/json")
    @GET("statistics")
    fun loadStatistics(): Call<Statistics>

    @Headers("Content-Type: application/json")
    @GET("dhm")
    fun loadDonorMilk(): Call<DHMModel>

    @Headers("Content-Type: application/json")
    @GET("milk-expression/{ip}")
    fun loadExpressedMilk(@Path("ip") ip: String): Call<MilkExpression>

    @Headers("Content-Type: application/json")
    @GET("hourly-feed/{ip}")
    fun loadFeedDistribution(@Path("ip") ip: String): Call<FeedsDistribution>

    @Headers("Content-Type: application/json")
    @GET("baby-growth/{ip}")
    fun loadWeights(@Path("ip") ip: String): Call<WeightsData>

}