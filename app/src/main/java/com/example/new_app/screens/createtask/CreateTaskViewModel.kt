package com.example.new_app.screens.createtask

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.TASK_DEFAULT_ID
import com.example.new_app.common.ext.idFromParameter
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.screens.TaskAppViewModel
import com.example.new_app.screens.tasklist.TaskListUiState
import com.example.new_app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateTaskViewModel() : TaskAppViewModel() {

    private val firebaseService: FirebaseService = FirebaseService()
    private val accountService: AccountService = AccountService()

    val task = mutableStateOf(Task())

    fun onTitleChange(newValue: String) {
        task.value = task.value.copy(title = newValue)
    }
    fun onDescriptionChange(newValue: String) {
        task.value = task.value.copy(description = newValue)
    }

    fun onIsCompletedChange(newValue: Boolean) {
        task.value = task.value.copy(isCompleted = newValue)
    }

    fun initialize(taskId: String) {
        launchCatching {
            if (taskId != TASK_DEFAULT_ID) {
                task.value = firebaseService.getTask(taskId.idFromParameter()) ?: Task()
            }
        }
    }

    fun onDoneClick(popUpScreen: () -> Unit) {
        viewModelScope.launch {
            try {
                val newTask = task.value.copy(
                    createdBy = accountService.currentUserId,
                    assignedTo = listOf(accountService.currentUserId),
                    isCompleted = false
                )
                firebaseService.save(newTask)
                popUpScreen()
            } catch (e: Exception) {
                // Handle the error
            }
        }
    }
}
