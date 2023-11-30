package com.example.framerunappfinal.UserRegistration

import androidx.room.Database
import androidx.room.RoomDatabase

//entity 는 todo 클래스
//RoomDatabase 라이브러리 사용
@Database(entities = arrayOf(User::class), version = 1)
abstract class UserDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}