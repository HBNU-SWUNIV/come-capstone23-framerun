package com.example.application.PasswordMode

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.databinding.ActivityPasswordmodeBinding
import com.example.application.putPasswordMode




class PasswordMode: AppCompatActivity() {

    private lateinit var binding: ActivityPasswordmodeBinding
    private val handler = Handler()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordmodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)


        // 최초 다운로드 시에만 mode2 스위치가 "On"으로 설정
        if (!sharedPreferences.getBoolean("initialDownload",false)) {
            setupInitialDownload()
        }

        // 상태 복원
        val isMode1Checked = sharedPreferences.getBoolean("mode1Checked", false)
        val isMode2Checked = sharedPreferences.getBoolean("mode2Checked", false)
        val isMode3Checked = sharedPreferences.getBoolean("mode3Checked", false)

        binding.mode1Sw.isChecked = isMode1Checked
        binding.mode2Sw.isChecked = isMode2Checked
        binding.mode3Sw.isChecked = isMode3Checked

        binding.mode1Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            if(binding.mode3Sw.isChecked){
                binding.mode1Sw.isChecked =false
            }else {
                saveSwitchState("mode1Checked", onSwitch)
                if (onSwitch) {
                    val mode = "mode_2"
                    putPasswordMode(mode)
                    binding.mode2Sw.isChecked = false
                    binding.mode3Sw.isChecked = false
                    Toast.makeText(this, "다중잠금 On", Toast.LENGTH_SHORT).show()

                } else {
                    binding.mode2Sw.isChecked = true
                    binding.mode3Sw.isChecked = false
                    Toast.makeText(this, "다중잠금 Off", Toast.LENGTH_SHORT).show()


                }
            }
        }

        binding.mode2Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            saveSwitchState("mode2Checked", onSwitch)
            if(onSwitch){
                val mode = "mode_1"
                putPasswordMode(mode)
                binding.mode1Sw.isChecked = false
                binding.mode3Sw.isChecked = false
                Toast.makeText(this,"기본잠금 On",Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(this,"기본잠금 Off",Toast.LENGTH_SHORT).show()


            }
        }

        binding.mode3Sw.setOnCheckedChangeListener { compoundButton, onSwitch ->
            if(binding.mode1Sw.isChecked){
                binding.mode3Sw.isChecked =false
                Toast.makeText(this, "다중잠금이 활성화되어 있습니다.", Toast.LENGTH_SHORT).show()
            }else {
                saveSwitchState("mode3Checked", onSwitch)
                if (onSwitch) {
                    binding.mode1Sw.isChecked = false
                    binding.mode2Sw.isChecked = false
                    startActivity(Intent(this, RandomPassword::class.java))
                    Toast.makeText(this, "임시비밀번호 On", Toast.LENGTH_SHORT).show()

                    handler.postDelayed({
                        if (binding.mode3Sw.isChecked) {
                            binding.mode2Sw.isChecked = true
                            binding.mode3Sw.isChecked = false
                            Toast.makeText(this, "임시비밀번호 종료", Toast.LENGTH_SHORT).show()
                        }
                    }, 10 * 1000)// 10분을 밀리초로 변환하여 지연 시간 설정
                } else {

                    binding.mode2Sw.isChecked = true
                    binding.mode1Sw.isChecked = false
                    Toast.makeText(this, "임시비밀번호 Off", Toast.LENGTH_SHORT).show()


                }
            }
        }

    }
    private fun setupInitialDownload() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("initialDownload", true) // 최초 다운로드 시 "On"으로 설정
        editor.apply()
    }
    private fun saveSwitchState(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
}