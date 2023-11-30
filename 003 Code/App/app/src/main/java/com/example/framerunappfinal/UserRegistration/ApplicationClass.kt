package com.example.framerunappfinal.UserRegistration

import android.app.Application
import com.example.framerunappfinal.CheckVisitor.IntruderDataRepository


//애플리케이션의 전역적인 설정이나 초기화 작업을 수행하기 위해 Application 클래스를 상속하고, UserRepository를 초기화하는 역할 수행
//애플리케이션 최초 실행 시 onCreate()메서드 호출되며, 여기서 필요한 초기화 작업을 수행함
class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()

        UserRepository.initialize(this)
        IntruderDataRepository.init(this)

    }
}