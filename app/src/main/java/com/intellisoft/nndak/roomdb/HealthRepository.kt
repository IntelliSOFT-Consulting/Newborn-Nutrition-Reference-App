package com.intellisoft.nndak.roomdb

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import com.intellisoft.nndak.helper_class.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


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
            val motherInfo = healthDao.getMotherInfo(nationalId)
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
            val motherInfo = healthDao.getMotherInfo(nationalId)
            if (motherInfo != null){
                val id = motherInfo.id.toString().toInt()
                healthDao.deleteMotherInfo(id)
            }



        }

    }



}