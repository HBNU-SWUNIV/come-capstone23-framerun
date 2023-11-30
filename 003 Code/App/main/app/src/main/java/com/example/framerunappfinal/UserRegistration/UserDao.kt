package com.example.framerunappfinal.UserRegistration

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    //Room 어노테이션을 사용해 구성
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dto: User)

    @Query("select * from userTable")
    fun list(): LiveData<MutableList<User>>
    //LiveData -> 변화하는 값 즉시반영 가능

    @Query("select * from userTable where id = (:id)")
    fun selectOne(id: Long): User

    @Update
    fun update(dto: User)
    //suspend fun update(dto: User)

    @Delete
    fun delete(dto: User)
}