package com.example.new_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _lastAddedTaskId = MutableLiveData<String?>()
    val lastAddedTaskId: LiveData<String?> get() = _lastAddedTaskId

    fun updateLastAddedTaskId(newTaskId: String?) {
        _lastAddedTaskId.value = newTaskId
    }
}
