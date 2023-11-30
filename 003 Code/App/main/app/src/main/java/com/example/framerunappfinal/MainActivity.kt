package com.example.framerunappfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.framerunappfinal.CheckVisitor.MyFirebaseMessagingService
import com.example.framerunappfinal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyFirebaseMessagingService().getFirebaseToken()
        val navController = findNavController(R.id.nav_fragment_activity_main)
        val bottomNavigationView = binding.navView

        // 바텀 네비게이션 뷰와 네비게이션 컨트롤러를 연동
        bottomNavigationView.setupWithNavController(navController)
    }
}