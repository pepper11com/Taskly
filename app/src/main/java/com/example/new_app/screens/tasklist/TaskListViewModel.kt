package com.example.new_app.screens.tasklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.CREATE_TASK_SCREEN
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.model.Task
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TaskListViewModel() : ViewModel() {

    private val firebaseService: FirebaseService = FirebaseService()

    private val _taskListUiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val taskListUiState: StateFlow<TaskListUiState> = _taskListUiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadTasks()
        }
    }

    private suspend fun loadTasks() {
        firebaseService.tasks
            .catch { e -> _taskListUiState.value = TaskListUiState(error = e.message) }
            .collect { tasks -> _taskListUiState.value = TaskListUiState(tasks = tasks, isLoading = false) }
    }

    fun onAddClick(openScreen: (String) -> Unit, userId: String) {
        openScreen("$CREATE_TASK_SCREEN$TASK_ID_KEY?userId=$userId")
    }

    fun onTaskClick(task: Task, openScreen: (String) -> Unit) {
        openScreen("${CREATE_TASK_SCREEN}/${task.id}")
    }

    fun onTaskDelete(task: Task) {
        viewModelScope.launch {
            firebaseService.delete(task.id)
            loadTasks()
        }
    }

    fun onTaskSwipeDeleted(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.DELETED))
            loadTasks()
        }
    }

    fun onTaskSwipeCompleted(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.COMPLETED))
            loadTasks()
        }
    }

    fun onTaskSwipeActive(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.ACTIVE))
            loadTasks()
        }
    }

}
