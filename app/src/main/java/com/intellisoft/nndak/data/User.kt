package com.intellisoft.nndak.data

import com.google.gson.annotations.SerializedName

data class User (
    @SerializedName("id") val userId: Int?,
    @SerializedName("user_email") val userEmail: String?,
    @SerializedName("user_password") val userPass: String?
)