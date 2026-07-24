package com.example.japanmap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TripDbEntity::class, PhotoDbEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
}
