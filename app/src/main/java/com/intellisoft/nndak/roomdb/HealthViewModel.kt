package com.intellisoft.nndak.roomdb

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intellisoft.nndak.helper_class.DbMotherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository

    init {
        val healthDao = HealthDatabase.getDatabase(application).healthDao()
        repository = HealthRepository(healthDao)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */


    fun insertMotherInfo(context: Context) {
        repository.insertMotherInfo(context)
    }
    fun deleteMotherInfo(context: Context)  {
        repository.deleteMotherInfo(context)
    }
    fun updateMotherInfo(context: Context, dbMotherInfo: DbMotherInfo){
        repository.updateMotherInfo(context, dbMotherInfo)
    }
    fun getMotherInfo(queryString: String, context: Context) = runBlocking {
        repository.getMotherInfo(queryString, context)
    }

}