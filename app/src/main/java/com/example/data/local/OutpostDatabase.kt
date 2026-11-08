package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OutpostStateEntity::class,
        BuildingEntity::class,
        WorkerClassEntity::class,
        SkillNodeEntity::class,
        EventLogEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class OutpostDatabase : RoomDatabase() {
    abstract fun outpostDao(): OutpostDao

    companion object {
        @Volatile
        private var INSTANCE: OutpostDatabase? = null

        fun getDatabase(context: Context): OutpostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OutpostDatabase::class.java,
                    "ashfall_outpost.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
