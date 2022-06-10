package com.intellisoft.nndak.api

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

}