package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppSettings::class, Shift::class, Employee::class, Attendance::class], version = 2, exportSchema = false)
abstract class HrDatabase : RoomDatabase() {
    abstract fun hrDao(): HrDao

    companion object {
        @Volatile
        private var INSTANCE: HrDatabase? = null

        fun getDatabase(context: Context): HrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HrDatabase::class.java,
                    "hr_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
