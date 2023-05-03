package com.example.new_app

import android.util.Log
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

    private val _mapsVisible = MutableLiveData(true)
    val mapsVisible: LiveData<Boolean> = _mapsVisible

    private val _initEdit = MutableLiveData(true)
    val initEdit: LiveData<Boolean> = _initEdit

    fun toggleInitEdit() {
        _initEdit.value = _initEdit.value?.not()
    }

    fun resetInitEdit() {
        _initEdit.value = true
    }

    fun toggleMapsVisible() {
        _mapsVisible.value = _mapsVisible.value?.not()
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
