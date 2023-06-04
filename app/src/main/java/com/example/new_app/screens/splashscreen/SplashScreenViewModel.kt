package com.example.new_app.screens.splashscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.AUTHENTICATION_SCREEN
import com.example.new_app.NAVIGATOR_SCREEN
import com.example.new_app.SPLASH_SCREEN
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.util.Resource
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.screens.task.tasklist.TaskListUiState
import com.example.new_app.screens.task.tasklist.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val accountService: AccountService,
    private val firebaseService: FirebaseService
) : ViewModel() {

    /**
     * Represents the loading state of the splash screen.
     * If true, a loading animation should be shown.
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Represents the current state of the task list loading process.
     * This includes loading, success, error, and empty states.
     */
    private val _taskListResource = MutableStateFlow<Resource<List<Task>>>(Resource.Loading())
    val taskListResource: StateFlow<Resource<List<Task>>> = _taskListResource.asStateFlow()

    private var snackbarShown = false


    /**
     * Initializes the ViewModel. Sets up a delay to control the loading animation.
     */
    init {
        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
        }
    }

    /**
     * Handles the app start event. This function is responsible for controlling the app's
     * navigation when it first starts. If the user is logged in, it navigates to the main
     * screen and loads tasks. Otherwise, it navigates to the authentication screen.
     */
    fun onAppStart(openAndPopUp: (String, String) -> Unit, clearBackstack: () -> Unit) {
        viewModelScope.launch {
            Log.d("SplashScreenViewModel", "onAppStart1: " + accountService.hasUser)
            if (accountService.hasUser) {
                clearBackstack()
                loadTasks()
                Log.d("SplashScreenViewModel", "onAppStart2: " + accountService.hasUser)
                openAndPopUp(NAVIGATOR_SCREEN, SPLASH_SCREEN)
                Log.d("SplashScreenViewModel", "onAppStart3: " + accountService.hasUser)
            } else {
                clearBackstack()
                openAndPopUp(AUTHENTICATION_SCREEN, SPLASH_SCREEN)
            }
        }
    }

    /**
     * Loads tasks from Firebase service, checks if tasks are overdue and
     * if yes, moves them to deleted tasks and shows a Snackbar message.
     */
    private suspend fun loadTasks() {
        firebaseService.tasks
            .catch { e -> _taskListResource.value = Resource.Error(e.message) }
            .first { tasks ->
                val currentTime = System.currentTimeMillis()

                tasks.forEach { task ->
                    task.dueDateToMillis()?.let { dueDateMillis ->
                        if (dueDateMillis < currentTime && task.status != TaskStatus.DELETED && task.status != TaskStatus.COMPLETED) {
                            updateTaskStatus(task, TaskStatus.DELETED)
                            if (!snackbarShown) {
                                SnackbarManager.showMessage("Some tasks were moved to deleted because they were overdue")
                                snackbarShown = true
                            }
                        }
                    }
                }
                _taskListResource.value = Resource.Success(tasks)
                true
            }
    }

    /**
     * Updates the status of the given task. The new status is given as a parameter.
     */
    private suspend fun updateTaskStatus(task: Task, status: TaskStatus) {
        firebaseService.updateTaskStatus(task.id, status)
    }
}
