package com.example.framerunappfinal.CheckVisitor

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object IntruderDataRepository {
    private lateinit var intruderDao: IntruderDao

    fun init(context: Context) {
        Log.d(TAG, "Initializing IntruderDataRepository")

        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "intruders-db"
        ).build()
        intruderDao = db.intruderDao()
    }

    fun getIntruders(): LiveData<List<Intruder>> = intruderDao.getAll()

    fun addIntruder(intruder: Intruder) {
        Log.d(TAG, "Adding intruder: $intruder")

        CoroutineScope(Dispatchers.IO).launch {
            intruderDao.insert(intruder)
        }
    }
}
