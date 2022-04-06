package com.intellisoft.nndak.api

import com.intellisoft.nndak.data.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface AuthService {

    @Headers("Content-Type: application/json")
    @POST("customer/signin")
    fun loginUser(@Body userData: User): Call<User>

    object ServiceBuilder {

        const val BASE_URL = "https://broshere.imejadevelopers.co.ke/api/"

        private val client = OkHttpClient.Builder().build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        fun<T> buildService(service: Class<T>): T{
            return retrofit.create(service)
        }
    }
}