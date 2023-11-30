package com.example.framerunappfinal.PasswordMode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.framerunappfinal.databinding.ActivityRandompasswordBinding
import com.example.framerunappfinal.putPasswordMode
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RandomPassword : AppCompatActivity() {


    private lateinit var binding: ActivityRandompasswordBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRandompasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //user_btn버튼 클릭 시 이벤트 설정
        binding.ranpassBtn.setOnClickListener {

            val pass_set: Set<Int> = randomNum()
            val mode = "mode_3"
            val hashedSet = bcrypt(pass_set)

            binding.ranpassText.text = pass_set.joinToString("")

            val curr = getCurrentTimePlus10Minutes()

            putPasswordMode(mode, hashedSet, curr)


        }
    }

    private fun randomNum() : Set<Int> {
        val pass_set = mutableSetOf<Int>()

        while(pass_set.size < 4) {
            pass_set.add((0..9).random())
        }
        return pass_set
    }


    private fun bcrypt(pass_set: Set<Int>) : String{

        val pass_set_hash = pass_set.joinToString("")
        val saltRounds = 12 // 해시에 사용할 salt 반복 횟수 (일반적으로 10 ~ 12 추천)
        val passwordHashed = BCrypt.hashpw(pass_set_hash, BCrypt.gensalt(saltRounds))

        return passwordHashed
    }

    // 현재 시간에 10분을 더한 값을 반환하는 함수
    private fun getCurrentTimePlus10Minutes(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 10)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}