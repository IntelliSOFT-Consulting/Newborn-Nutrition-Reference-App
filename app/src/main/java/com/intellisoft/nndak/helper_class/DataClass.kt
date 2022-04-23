package com.intellisoft.nndak.helper_class

enum class DbMotherKey {
    NATIONALID, FHIRID
}
data class DbMotherInfo(
    val nationalId:String,
    val motherDob:String,
    val firstName:String,
    val familyName:String,
    val phoneNumber:String,
    val fhirId:String,
    
)