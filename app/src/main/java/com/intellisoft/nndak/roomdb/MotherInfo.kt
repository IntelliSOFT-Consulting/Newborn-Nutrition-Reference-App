package com.intellisoft.nndak.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mother_info")
data class MotherInfo(

        val nationalId: String,
        val motherDob: String = "",
        val firstName: String = "",
        val familyName: String = "",
        val phoneNumber: String = "",
        val fhirId: String = "",


        ){

        @PrimaryKey(autoGenerate = true)
        var id: Int? = null
}