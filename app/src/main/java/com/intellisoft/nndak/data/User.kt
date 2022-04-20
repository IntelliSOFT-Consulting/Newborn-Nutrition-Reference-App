package com.intellisoft.nndak.data

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?
)

data class AuthResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("issued") val issued: String?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("message") val message: String?,
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
