package com.example.new_app.screens.task.create_edit_tasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.snackbar.SnackbarMessage
import com.example.new_app.common.util.Resource
import com.example.new_app.model.CustomLatLng
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.model.service.scheduleTaskReminder
import com.example.new_app.screens.TaskAppViewModel
import com.example.new_app.screens.task.tasklist.generateStaticMapUrl
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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

    val locationDisplay = mutableStateOf("")

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
        locationDisplay.value = ""
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

    private suspend fun compressImage(context: Context, uri: Uri): Uri {
        return withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader(context)
            val size = 720
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(size)
                .build()

            val result = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
            val compressedFile = File.createTempFile("compressed", ".jpg", context.cacheDir)

            when (result?.byteCount!!){
                in 0..5000000 -> {
                    FileOutputStream(compressedFile).use { fileOutputStream ->
                        result.compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream)
                    }
                }
                in 5000001..10000000 -> {
                    FileOutputStream(compressedFile).use { fileOutputStream ->
                        result.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
                    }
                }
                else -> {
                    FileOutputStream(compressedFile).use { fileOutputStream ->
                        result.compress(Bitmap.CompressFormat.JPEG, 25, fileOutputStream)
                    }
                }
            }

            Uri.fromFile(compressedFile)
        }
    }

    private suspend fun uploadImageAndSaveTask(newTask: Task, taskId: String, userId: String, imageUri: Uri, context: Context): Task {
        val path = "task_images/$userId/$taskId"
        val compressedImageUri = compressImage(context, imageUri)
        val imageUrl = firebaseService.uploadImage(compressedImageUri, path)
        return newTask.copy(imageUri = imageUrl)
    }


    fun onDoneClick(context: Context, taskId: String?, popUpScreen: () -> Unit, onTaskCreated: (String) -> Unit) {
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
                                        imageUri,
                                        context
                                    )
                                }

                                // Update the task with the new imageUri
                                if (updatedTask != null) {
                                    firebaseService.updateTask(updatedTask)
                                }
                            }
                            var imageUrl: String? = null
                            if (task.value.location?.latitude != null && task.value.location?.longitude != null) {
                                imageUrl = generateStaticMapUrl(task.value)
                            }

                            onTaskCreated(savedTaskId!!)
                            task.value.dueDateToMillis()?.let { dueDateMillis ->
                                scheduleTaskReminder(savedTaskId, "${task.value.title} at: ${task.value.dueTime}", task.value.locationName ?: task.value.description, dueDateMillis, imageUrl, context)
                            }

                            Log.d("TaskEditCreateViewModel", "Task created successfully with id: ----  $savedTaskId  -------")
                            resetTask()
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
                            imageUri,
                            context
                        )
                        when (val updateResult = firebaseService.updateTask(updatedTask)) {
                            is Resource.Success -> {
                                onTaskCreated(taskId)
                                Log.d("TaskEditCreateViewModel", "Task EDITED successfully with id: ----  $taskId  -------")

                                var imageUrl: String? = null
                                if (task.value.location?.latitude != null && task.value.location?.longitude != null) {
                                    imageUrl = generateStaticMapUrl(task.value)
                                }

                                task.value.dueDateToMillis()?.let { dueDateMillis ->
                                    scheduleTaskReminder(taskId, "${task.value.title} at: ${task.value.dueTime}", task.value.locationName ?: task.value.description, dueDateMillis, imageUrl, context)
                                }

                                resetTask()
                            }
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
                            is Resource.Success -> {
                                var imageUrl: String? = null
                                if (task.value.location?.latitude != null && task.value.location?.longitude != null) {
                                    imageUrl = generateStaticMapUrl(task.value)
                                }
                                task.value.dueDateToMillis()?.let { dueDateMillis ->
                                    scheduleTaskReminder(taskId, "${task.value.title} at: ${task.value.dueTime}", task.value.locationName ?: task.value.description, dueDateMillis, imageUrl, context)
                                }
                            }
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
            imageUri.value = null
        }
    }

    fun resetTask() {
        task.value = Task()
        imageUri.value = null
        onLocationReset()
    }

    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }
}