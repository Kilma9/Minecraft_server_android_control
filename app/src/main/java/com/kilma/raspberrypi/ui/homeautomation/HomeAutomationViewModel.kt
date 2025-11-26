package com.kilma.raspberrypi.ui.homeautomation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeAutomationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Home Automation"
    }
    val text: LiveData<String> = _text
}
