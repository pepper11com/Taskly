package com.example.new_app.screens.task.create_edit_tasks

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.common.util.Resource
import com.example.new_app.model.CustomLatLng
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.screens.TaskAppViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskEditCreateViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val accountService: AccountService,
    private val storage: FirebaseStorage,
    private val snackbarManager: SnackbarManager
) : TaskAppViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"

    val task = mutableStateOf(Task())
    val imageUri = mutableStateOf<String?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    var marker: Marker? = null

    private val _taskEditCreateState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val taskEditCreateState: StateFlow<Resource<Unit>> get() = _taskEditCreateState


    fun initialize(taskId: String?) {
        viewModelScope.launch {
            taskId?.let {
                if (taskId.isNotEmpty()) {
                    task.value = firebaseService.getTask(taskId) ?: Task()
                }
            }
        }
    }

    fun onTitleChange(newValue: String) {
        task.value = task.value.copy(title = newValue)
    }

    fun onDescriptionChange(newValue: String) {
        task.value = task.value.copy(description = newValue)
    }

    fun onLocationChange(latLng: LatLng) {
        task.value = task.value.copy(location = CustomLatLng(latLng.latitude, latLng.longitude))
    }

    fun onLocationNameChange(newValue: String) {
        task.value = task.value.copy(locationName = newValue)
    }

    fun onLocationReset() {
        marker?.remove()
        task.value = task.value.copy(location = null)
        task.value = task.value.copy(locationName = null)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onDateChange(newValue: Long) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()))
        calendar.timeInMillis = newValue
        val newDueDate = SimpleDateFormat(dateFormat, Locale.ENGLISH).format(calendar.time)
        task.value = task.value.copy(dueDate = newDueDate)
    }

    fun onTimeChange(hour: Int, minute: Int) {
        val newDueTime = "${hour.toClockPattern()}:${minute.toClockPattern()}"
        task.value = task.value.copy(dueTime = newDueTime)
    }

    fun onImageChange(newValue: Uri) {
        imageUri.value = newValue.toString()
    }

    suspend fun uploadImageAndSaveTask(
        newTask: Task,
        taskId: String,
        userId: String,
        imageUri: Uri
    ): Task {
        val path = "task_images/$userId/$taskId"
        println("path: $path")
        val imageUrl = firebaseService.uploadImage(imageUri, path)
        return newTask.copy(imageUri = imageUrl)
    }


    fun onDoneClick(taskId: String?, popUpScreen: () -> Unit) {
        viewModelScope.launch {
            _taskEditCreateState.value = Resource.Loading()
            try {
                if (taskId == null) {
                    val newTask = task.value.copy(
                        createdBy = accountService.currentUserId,
                        assignedTo = listOf(accountService.currentUserId),
                        isCompleted = false
                    )

                    // Save the task first to get the task ID
                    when (val saveResult = firebaseService.save(newTask)) {
                        is Resource.Success -> {
                            val savedTaskId = saveResult.data

                            // Check if there is an image to upload
                            if (imageUri.value != null) {
                                val imageUri = Uri.parse(imageUri.value)
                                val updatedTask = savedTaskId?.let {
                                    uploadImageAndSaveTask(
                                        newTask.copy(id = savedTaskId),
                                        it,
                                        accountService.currentUserId,
                                        imageUri
                                    )
                                }

                                // Update the task with the new imageUri
                                if (updatedTask != null) {
                                    firebaseService.updateTask(updatedTask)
                                }
                            }
                        }
                        is Resource.Error -> {
                            _taskEditCreateState.value = Resource.Error(saveResult.message ?: "Unknown error")
                            snackbarManager.showSnackbarMessage(
                                SnackbarMessage.Text(saveResult.message ?: "Unknown error")
                            )
                            return@launch
                        }
                        else -> {
                            _taskEditCreateState.value = Resource.Error("Unknown error")
                            snackbarManager.showSnackbarMessage(
                                SnackbarMessage.Text("Unknown error")
                            )
                            return@launch
                        }
                    }

                } else {
                    if (imageUri.value != null) {
                        val imageUri = Uri.parse(imageUri.value)
                        val updatedTask = uploadImageAndSaveTask(
                            task.value,
                            taskId,
                            accountService.currentUserId,
                            imageUri
                        )
                        when (val updateResult = firebaseService.updateTask(updatedTask)) {
                            is Resource.Success -> { /* Task updated successfully */ }
                            is Resource.Error -> {
                                _taskEditCreateState.value = Resource.Error(updateResult.message ?: "Unknown error")
                                snackbarManager.showSnackbarMessage(
                                    SnackbarMessage.Text(updateResult.message ?: "Unknown error")
                                )
                                return@launch
                            }
                            else -> {
                                _taskEditCreateState.value = Resource.Error("Unknown error")
                                snackbarManager.showSnackbarMessage(
                                    SnackbarMessage.Text("Unknown error")
                                )
                                return@launch
                            }
                        }
                    } else {
                        when (val updateResult = firebaseService.updateTask(task.value)) {
                            is Resource.Success -> { /* Task updated successfully */ }
                            is Resource.Error -> {
                                _taskEditCreateState.value = Resource.Error(updateResult.message ?: "Unknown error")
                                snackbarManager.showSnackbarMessage(
                                    SnackbarMessage.Text(updateResult.message ?: "Unknown error")
                                )
                                return@launch
                            }
                            else -> {
                                _taskEditCreateState.value = Resource.Error("Unknown error")
                                snackbarManager.showSnackbarMessage(
                                    SnackbarMessage.Text("Unknown error")
                                )
                                return@launch
                            }
                        }
                    }
                }
                _taskEditCreateState.value = Resource.Success(Unit)
                snackbarManager.showSnackbarMessage(
                    SnackbarMessage.Text("Task successfully saved")
                )
                popUpScreen()
            } catch (e: Exception) {
                _taskEditCreateState.value = Resource.Error(e.message ?: "Unknown error")
                snackbarManager.showSnackbarMessage(
                    SnackbarMessage.Text(e.message ?: "Unknown error")
                )
            } finally {
                _taskEditCreateState.value = Resource.Empty()
            }
        }
    }



    fun onDeleteImageClick() {
        viewModelScope.launch {
            task.value.imageUri?.let { imageUrl ->
                val path = imageUrl.substringAfter("task_images/")
                firebaseService.deleteImage(path)
                task.value = task.value.copy(imageUri = null)
                imageUri.value = null
            }
        }
    }


    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }
}