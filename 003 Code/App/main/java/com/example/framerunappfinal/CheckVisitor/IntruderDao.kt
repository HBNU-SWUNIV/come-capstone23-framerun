package com.example.framerunappfinal.CheckVisitor

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IntruderDao {
    @Query("SELECT * FROM intruder")
    fun getAll(): LiveData<List<Intruder>>

    @Insert
    fun insert(intruder: Intruder)
}
