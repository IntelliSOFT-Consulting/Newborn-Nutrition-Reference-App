package com.intellisoft.nndak.roomdb

import android.content.Context
import android.util.Log

import com.intellisoft.nndak.helper_class.*
import kotlinx.coroutines.*


class HealthRepository(private val healthDao: HealthDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    private fun getSharedPref(context: Context, sharedKey: String): String? {
        return FormatHelper().retrieveSharedPreference(context, sharedKey)
    }


    fun insertMotherInfo(context: Context){

        CoroutineScope(Dispatchers.IO).launch {

            val natID = DbMotherKey.NATIONALID.name
            val nationalId = getSharedPref(context, natID)

            if (nationalId != null){

                //Check if id number is in the system
                val isMother = healthDao.checkMotherInfo(nationalId)
                if (!isMother){

                    val motherInfo = MotherInfo(nationalId)
                    healthDao.addMotherInfo(motherInfo)
                }

            }


        }

    }

    fun updateMotherInfo(context: Context, dbMotherInfo:DbMotherInfo){

        CoroutineScope(Dispatchers.IO).launch {

            val nationalId = dbMotherInfo.nationalId
            val motherInfo = healthDao.getMotherInfoNational(nationalId)
            val motherDob = dbMotherInfo.motherDob


            val firstName = dbMotherInfo.firstName
            val familyName = dbMotherInfo.familyName
            val phoneNumber = dbMotherInfo.phoneNumber
            val fhirId = dbMotherInfo.fhirId

            if (motherInfo != null){

                val id = motherInfo.id.toString().toInt()
                healthDao.updateMotherInfo(fhirId, phoneNumber, firstName, familyName, motherDob, id)

            }else{

                val motherDataInfo = MotherInfo(nationalId, motherDob, firstName, familyName, phoneNumber, fhirId)
                healthDao.addMotherInfo(motherDataInfo)
            }



        }

    }

    fun deleteMotherInfo(context: Context){

        CoroutineScope(Dispatchers.IO).launch {

            val natID = DbMotherKey.NATIONALID.name
            val nationalId = getSharedPref(context, natID).toString()
            val motherInfo = healthDao.getMotherInfoNational(nationalId)
            if (motherInfo != null){
                val id = motherInfo.id.toString().toInt()
                healthDao.deleteMotherInfo(id)
            }



        }

    }

    suspend fun getMotherInfo(queryString: String, context: Context): MotherInfo? {
        return getMotherData(queryString, context)
    }

    private suspend fun getMotherData(queryString: String,
                                      context: Context):MotherInfo?{

        val queryValue = FormatHelper().retrieveSharedPreference(
                context,
                "queryValue").toString()

        var motherInfo : MotherInfo? = null
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch{

            when (queryString) {
                DbMotherKey.NATIONALID.name -> {
                    motherInfo = healthDao.getMotherInfoNational(queryValue)
                }
                DbMotherKey.PHONE_NUMBER.name -> {
                    motherInfo = healthDao.getMotherInfoPhone(queryValue)
                }
                DbMotherKey.MOTHER_DOB.name -> {
                    motherInfo = healthDao.getMotherInfoMotherDoB(queryValue)
                }
            }

        }.join()

        return motherInfo

    }


}