package com.example.new_app.screens.task.tasklist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.CREATE_TASK_SCREEN
import com.example.new_app.TASK_ID_KEY
import com.example.new_app.common.sort.TaskSortType
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val accountService: AccountService
) : ViewModel() {


    private val _taskListUiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val taskListUiState: StateFlow<TaskListUiState> = _taskListUiState.asStateFlow()

    private val _deleteTasksState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val deleteTasksState: StateFlow<Resource<Unit>> get() = _deleteTasksState

    private val _sortType = MutableStateFlow(TaskSortType.DUE_DATE_ASC)
    val sortType: StateFlow<TaskSortType> = _sortType.asStateFlow()

    val currentUserId: String
        get() = accountService.currentUserId


    init {
        viewModelScope.launch {
            loadTasks()
        }
    }

    private suspend fun loadTasks() {
        firebaseService.tasks
            .catch { e -> _taskListUiState.value = TaskListUiState(error = e.message) }
            .collect { tasks ->
                _taskListUiState.value = TaskListUiState(tasks = tasks, isLoading = false)
            }
    }

    fun updateSortType(sortType: TaskSortType) {
        _sortType.value = sortType
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
        }
    }

    fun onTaskSwipeCompleted(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.COMPLETED))
        }
    }

    fun onTaskSwipeActive(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.ACTIVE))
        }
    }

//    fun onDeleteSelectedTasks(tasks: List<Task>) {
//        viewModelScope.launch {
//            Log.d("TaskListViewModel", "onDeleteSelectedTasks: ${tasks.size}")
//            tasks.forEach { task ->
//                launch {
//                    firebaseService.delete(task.id)
//                }
//            }
//        }
//    }

    fun onDeleteSelectedTasks(taskIds: List<String>) {
        viewModelScope.launch {
            Log.d("TaskListViewModel", "onDeleteSelectedTasks: ${taskIds.size}")
            taskIds.forEach { taskId ->
                launch {
                    firebaseService.delete(taskId)
                }
            }
        }
    }


    fun resetDeleteTasksState() {
        _deleteTasksState.value = Resource.Empty()
    }


}
