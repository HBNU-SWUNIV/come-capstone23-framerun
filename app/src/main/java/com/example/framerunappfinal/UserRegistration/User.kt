package com.example.framerunappfinal.UserRegistration

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//@Entity -> 테이블 이름
@Entity(tableName = "userTable")
class User(
    //@ColumnInfo -> 컬럼에 들어갈 이름
    //@PrimaryKey -> 기본키
    //autoGenerate = true -> id 값을 자동으로 증가시킴
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "timestamp") val timestamp: String,
    @ColumnInfo(name = "isChecked") var isChecked: Boolean
): Serializable {
}
//intent에 객체를 담기위해 Serializable을 상속받음