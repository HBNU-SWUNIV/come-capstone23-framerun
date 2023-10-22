package com.example.application.UserRegistration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.application.databinding.ActivityUserRegistrationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRegistrationScreen : AppCompatActivity() {
    lateinit var binding: ActivityUserRegistrationBinding
    lateinit var userViewModel: UserViewModel
    lateinit var userAdapter: UserAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //수정 2차 시도
        UserRepository.initialize(applicationContext) // UserRepository 초기화 추가

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        userViewModel.userList.observe(this) {
            userAdapter.update(it)
        }

        userAdapter = UserAdapter(this)
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        binding.rvUserList.adapter = userAdapter


        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java).apply {
                putExtra("type", "ADD")
            }
            requestActivity.launch(intent)
        }

    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val user = it.data?.getSerializableExtra("user") as User

            when(it.data?.getIntExtra("flag", -1)) {
                0 -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        userViewModel.insert(user)
                    }
                    Toast.makeText(this, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}