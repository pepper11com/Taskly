package com.example.new_app.screens.task.tasklist

import android.content.Context
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
import com.example.new_app.model.service.cancelTaskReminder
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

    /**
     * State for the UI, which shows a list of tasks and loading/error states
     */
    private val _taskListUiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val taskListUiState: StateFlow<TaskListUiState> = _taskListUiState.asStateFlow()

    /**
     * State for tracking the delete task operation
     */
    private val _deleteTasksState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val deleteTasksState: StateFlow<Resource<Unit>> get() = _deleteTasksState

    /**
     * State for the selected task sort type
     */
    private val _sortType = MutableStateFlow(TaskSortType.DUE_DATE_ASC)
    val sortType: StateFlow<TaskSortType> = _sortType.asStateFlow()

    /**
     * Returns the current user's ID
     */
    val currentUserId: String
        get() = accountService.currentUserId

    /**
     * On initialization, the ViewModel loads the tasks
     */
    init {
        viewModelScope.launch {
            loadTasks()
        }
    }

    /**
     * Loads the tasks from the Firebase service and updates the UI state
     */
    private suspend fun loadTasks() {
        firebaseService.tasks
            .catch { e -> _taskListUiState.value = TaskListUiState(error = e.message) }
            .collect { tasks ->
                _taskListUiState.value = TaskListUiState(tasks = tasks, isLoading = false)
            }
    }

    /**
     * Updates the selected sort type
     */
    fun updateSortType(sortType: TaskSortType) {
        _sortType.value = sortType
    }

    /**
     * Handles the add button click event by opening the create task screen
     */
    fun onAddClick(openScreen: (String) -> Unit, userId: String) {
        openScreen("$CREATE_TASK_SCREEN$TASK_ID_KEY?userId=$userId")
    }

    /**
     * Handles the task delete operation and updates the tasks
     */
    fun onTaskDelete(task: Task, context: Context) {
        viewModelScope.launch {
            firebaseService.delete(task.id)
            cancelTaskReminder(task.id, context)
            loadTasks()
        }
    }

    /**
     * Handles the task swipe delete operation by marking the task as deleted
     */
    fun onTaskSwipeDeleted(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.DELETED))
        }
    }

    /**
     * Handles the task swipe complete operation by marking the task as completed
     */
    fun onTaskSwipeCompleted(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.COMPLETED))
        }
    }

    /**
     * Handles the task swipe active operation by marking the task as active
     */
    fun onTaskSwipeActive(task: Task) {
        viewModelScope.launch {
            firebaseService.updateTask(task.copy(status = TaskStatus.ACTIVE))
        }
    }

    /**
     * Handles the delete selected tasks operation and updates the tasks
     */
    fun onDeleteSelectedTasks(taskIds: List<String>, context: Context) {
        viewModelScope.launch {
            Log.d("TaskListViewModel", "onDeleteSelectedTasks: ${taskIds.size}")
            taskIds.forEach { taskId ->
                launch {
                    firebaseService.delete(taskId)
                    cancelTaskReminder(taskId, context)
                }
            }
        }
    }

    /**
     * Resets the delete tasks state
     */
    fun resetDeleteTasksState() {
        _deleteTasksState.value = Resource.Empty()
    }
}
