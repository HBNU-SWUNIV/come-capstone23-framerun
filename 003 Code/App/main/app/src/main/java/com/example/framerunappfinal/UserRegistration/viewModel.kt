package com.example.framerunappfinal.UserRegistration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//액티비티의 라이프사이클과 별개로 작동
//데이터 유지 및 공유 가능
class UserViewModel: ViewModel() {
    val userList: LiveData<MutableList<User>>
    private val userRepository: UserRepository = UserRepository.get()

    init {
        userList = userRepository.list()
    }

    fun getOne(id: Long) = userRepository.getUser(id)

    fun insert(dto: User) = viewModelScope.launch(Dispatchers.IO) {
        userRepository.insert(dto)
    }

    fun update(dto: User) = viewModelScope.launch(Dispatchers.IO) {
        userRepository.update(dto)
    }

    fun delete(dto: User) = viewModelScope.launch(Dispatchers.IO) {
        userRepository.delete(dto)
    }

}