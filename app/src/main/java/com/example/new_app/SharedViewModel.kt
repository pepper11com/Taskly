package com.example.new_app

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {

    private val _lastAddedTaskId = MutableLiveData<String?>()
    val lastAddedTaskId: LiveData<String?> get() = _lastAddedTaskId

    private val _selectedTaskIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTaskIds: StateFlow<Set<String>> = _selectedTaskIds.asStateFlow()

    val visiblePermissionDialog = mutableStateListOf<String>()

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean,
    ){
        if(!isGranted){
            visiblePermissionDialog.add(0, permission)
        }
    }

    fun updateLastAddedTaskId(newTaskId: String?) {
        _lastAddedTaskId.value = newTaskId
    }

    fun onTaskSelection(taskId: String, isSelected: Boolean) {
        _selectedTaskIds.value = if (isSelected) {
            _selectedTaskIds.value + taskId
        } else {
            _selectedTaskIds.value - taskId
        }

        Log.d("SharedViewModel", "onTaskSelection: ${_selectedTaskIds.value} and $taskId and the length is ${_selectedTaskIds.value.size}")
    }

    fun clearSelectedTaskIds() {
        _selectedTaskIds.value = emptySet()
    }
}
