package com.intellisoft.nndak.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface HealthDao {

    @Insert
    suspend fun addMotherInfo(motherInfo: MotherInfo)

    @Query("SELECT EXISTS (SELECT 1 FROM mother_info WHERE nationalId LIKE :nationalId)")
    fun checkMotherInfo(nationalId:String): Boolean

    @Query("UPDATE mother_info SET fhirId =:fhirId AND phoneNumber =:phoneNumber AND firstName =:firstName AND familyName =:familyName AND motherDob =:motherDob WHERE id LIKE :id")
    suspend fun updateMotherInfo(
        fhirId: String,
        phoneNumber: String,
        firstName: String,
        familyName: String,
        motherDob: String,
        id: Int)

    @Query("SELECT * from mother_info WHERE nationalId LIKE :nationalId")
    suspend fun getMotherInfo(nationalId: String): MotherInfo?

    @Query("DELETE FROM mother_info WHERE id =:id")
    suspend fun deleteMotherInfo(id: Int)

}