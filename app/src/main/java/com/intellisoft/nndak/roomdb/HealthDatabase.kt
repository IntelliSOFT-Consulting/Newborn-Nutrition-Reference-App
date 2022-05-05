package com.intellisoft.nndak.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
        entities = [
            MotherInfo::class
        ],
        version = 1,
        exportSchema = false)
public abstract class HealthDatabase : RoomDatabase() {

    abstract fun healthDao() : HealthDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        HealthDatabase::class.java,
                        "health_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}