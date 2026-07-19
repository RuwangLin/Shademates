package com.example.a5120_shademate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SuburbWeather::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shademate_database"
                )
                .fallbackToDestructiveMigration() // Recreate the local cache if the schema version changes.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
