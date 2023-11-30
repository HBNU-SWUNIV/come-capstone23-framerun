package com.example.framerunappfinal.CheckVisitor

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Intruder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intruderDao(): IntruderDao
}