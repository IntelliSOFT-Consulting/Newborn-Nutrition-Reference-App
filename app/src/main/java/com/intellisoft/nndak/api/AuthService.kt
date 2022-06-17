package com.intellisoft.nndak.api

import com.intellisoft.nndak.charts.DHMModel
import com.intellisoft.nndak.charts.Statistics
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
    @GET("auth/statistics")
    fun loadStatistics(): Call<Statistics>


    @Headers("Content-Type: application/json")
    @GET("auth/dhm")
    fun loadDonorMilk(): Call<DHMModel>

}