package com.intellisoft.nndak.data

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?
)

data class AuthResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("issued") val issued: String?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("newUser") val newUser: Boolean?,
    @SerializedName("_reset_url") val _reset_url: String?,

    )

data class UserResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: User

)

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("names") val names: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String

)

data class SessionData(
    @SerializedName("patientId") val patientId: String,
    @SerializedName("status") val status: Boolean,
)

data class DHMStock(
    @SerializedName("pasteurized") val pasteurized: String,
    @SerializedName("unPasteurized") val unPasteurized: String,
    @SerializedName("PretermPasteurized") val PretermPasteurized: String,
    @SerializedName("PretermunPasteurized") val PretermunPasteurized: String,
    @SerializedName("userId") val userId: String

)

data class DispenseData(
    @SerializedName("dhmType") val dhmType: String,
    @SerializedName("dhmVolume") val dhmVolume: String,
    @SerializedName("remarks") val remarks: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("category") val category: String

)