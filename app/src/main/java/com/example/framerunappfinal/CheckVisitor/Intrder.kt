package com.example.framerunappfinal.CheckVisitor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Intruder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "title") val title: String
)