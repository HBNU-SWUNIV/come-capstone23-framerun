package com.example.framerunappfinal.CheckVisitor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CheckVisitorViewModel : ViewModel() {
    val intruders: LiveData<List<Intruder>>

    init {
        intruders = IntruderDataRepository.getIntruders()
    }

    fun addIntruder(image: String, title: String) {
        IntruderDataRepository.addIntruder(Intruder(image = image, title = title))
    }
}