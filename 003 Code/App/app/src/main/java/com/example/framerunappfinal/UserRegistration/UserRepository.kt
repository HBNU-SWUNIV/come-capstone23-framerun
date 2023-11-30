package com.example.framerunappfinal.UserRegistration

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
private const val DATABASE_NAME = "user-database.db"

class UserRepository private constructor(context: Context){

    //데이터베이스 빌드
    private val database: UserDatabase = Room.databaseBuilder(
        context.applicationContext,
        UserDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val userDao = database.userDao()

    fun list(): LiveData<MutableList<User>> = userDao.list()

    fun getUser(id: Long): User = userDao.selectOne(id)

    fun insert(dto: User) = userDao.insert(dto)

    suspend fun update(dto: User) = userDao.update(dto)

    fun delete(dto: User) = userDao.delete(dto)

    //클래스가 생성될 때 메모리에 적재되면서 동시에 생성하는 객체
    //데이터베이스 생성 및 초기화를 담당
    companion object {
        private var INSTANCE: UserRepository?=null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(context)
            }
        }

        fun get(): UserRepository {
            return INSTANCE ?:
            throw IllegalStateException("UserRepository must be initialized")
        }
    }
}