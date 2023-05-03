package com.example.new_app.screens.task.create_edit_tasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.new_app.common.snackbar.SnackbarManager
import com.example.new_app.common.util.Resource
import com.example.new_app.model.CustomLatLng
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.model.service.cancelTaskReminder
import com.example.new_app.model.service.scheduleTaskReminder
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class TaskEditCreateViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val accountService: AccountService,
) : ViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"
    val alertTimeDisplay = mutableStateOf("1 hour in advance")

    val task = mutableStateOf(Task())
    val imageUri = mutableStateOf<String?>(null)
    var marker: Marker? = null

    private val _taskEditCreateState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val taskEditCreateState: StateFlow<Resource<Unit>> get() = _taskEditCreateState

    val locationDisplay = mutableStateOf("")

    fun initialize(taskId: String?) {
        viewModelScope.launch {
            taskId?.let {
                if (taskId.isNotEmpty()) {
                    task.value = firebaseService.getTask(taskId) ?: Task()

                    task.value.let {
                        locationDisplay.value = it.locationName.toString()
                    }

                    task.value.let {
                        imageUri.value = it.imageUri
                    }

                    alertTimeToText(task.value.alertMessageTimer)
                }
            }
        }
    }

    fun onAlertOptionChange(option: String) {
        alertTimeDisplay.value = option
        task.value = task.value.copy(alertMessageTimer = alertTimeToMillis(option))
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

    fun onColorChange(newColor: Int) {
        task.value = task.value.copy(color = newColor)
    }

    fun onLocationReset() {
        marker?.remove()
        task.value = task.value.copy(location = null)
        task.value = task.value.copy(locationName = null)
        locationDisplay.value = ""
    }

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

            when (result?.byteCount!!) {
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

    private suspend fun uploadImageAndSaveTask(
        newTask: Task,
        taskId: String,
        userId: String,
        imageUri: Uri,
        context: Context
    ): Task {
        val path = "task_images/$userId/$taskId"
        val compressedImageUri = compressImage(context, imageUri)
        val imageUrl = firebaseService.uploadImage(compressedImageUri, path)
        return newTask.copy(imageUri = imageUrl)
    }

    private suspend fun createNewTask(
        context: Context,
        popUpScreen: () -> Unit,
        onTaskCreated: (String) -> Unit
    ) {
        val newTask = task.value.copy(
            createdBy = accountService.currentUserId,
            assignedTo = listOf(accountService.currentUserId),
            isCompleted = false
        )
        // Save the task first to get the task ID
        when (val saveResult = firebaseService.save(newTask)) {
            is Resource.Success -> {
                popUpScreen()

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
                    val notificationId = generateUniqueNotificationId()
                    scheduleTaskReminder(
                        savedTaskId,
                        "${task.value.title} at: ${task.value.dueTime}",
                        task.value.locationName ?: task.value.description,
                        dueDateMillis,
                        imageUrl,
                        task.value.alertMessageTimer,
                        notificationId,
                        context
                    )
                }

                resetTask()
            }

            is Resource.Error -> {
                _taskEditCreateState.value = Resource.Error(saveResult.message ?: "Unknown error")
                SnackbarManager.showMessage(saveResult.message ?: "Unknown error")
            }

            else -> {
                _taskEditCreateState.value = Resource.Error("Unknown error")
                SnackbarManager.showMessage("Unknown error")
            }
        }
    }

    private suspend fun editExistingTask(
        context: Context,
        taskId: String,
        popUpScreen: () -> Unit,
        onTaskCreated: (String) -> Unit
    ) {
        // Move the code for editing an existing task from the onDoneClick function here
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
                    popUpScreen()
                    onTaskCreated(taskId)
                    var imageUrl: String? = null
                    if (task.value.location?.latitude != null && task.value.location?.longitude != null) {
                        imageUrl = generateStaticMapUrl(task.value)
                    }

                    // Cancel the previous task reminder
                    cancelTaskReminder(taskId, context)

                    task.value.dueDateToMillis()?.let { dueDateMillis ->
                        val notificationId = generateUniqueNotificationId()
                        scheduleTaskReminder(
                            taskId,
                            "${task.value.title} at: ${task.value.dueTime}",
                            task.value.locationName ?: task.value.description,
                            dueDateMillis,
                            imageUrl,
                            task.value.alertMessageTimer,
                            notificationId,
                            context
                        )
                    }
                    resetTask()
                }

                is Resource.Error -> {
                    _taskEditCreateState.value =
                        Resource.Error(updateResult.message ?: "Unknown error")
                    SnackbarManager.showMessage(updateResult.message ?: "Unknown error")
                }

                else -> {
                    _taskEditCreateState.value = Resource.Error("Unknown error")
                    SnackbarManager.showMessage("Unknown error")
                }
            }
        } else {
            when (val updateResult = firebaseService.updateTask(task.value)) {
                is Resource.Success -> {
                    popUpScreen()
                    var imageUrl: String? = null
                    if (task.value.location?.latitude != null && task.value.location?.longitude != null) {
                        imageUrl = generateStaticMapUrl(task.value)
                    }
                    task.value.dueDateToMillis()?.let { dueDateMillis ->
                        val notificationId = generateUniqueNotificationId()
                        scheduleTaskReminder(
                            taskId,
                            "${task.value.title} at: ${task.value.dueTime}",
                            task.value.locationName ?: task.value.description,
                            dueDateMillis,
                            imageUrl,
                            task.value.alertMessageTimer,
                            notificationId,
                            context
                        )
                    }
                    resetTask()
                }

                is Resource.Error -> {
                    _taskEditCreateState.value =
                        Resource.Error(updateResult.message ?: "Unknown error")
                    SnackbarManager.showMessage(updateResult.message ?: "Unknown error")
                }

                else -> {
                    _taskEditCreateState.value = Resource.Error("Unknown error")
                    SnackbarManager.showMessage("Unknown error")
                }
            }
        }
    }

    fun onDoneClick(
        context: Context,
        taskId: String?,
        popUpScreen: () -> Unit,
        onTaskCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _taskEditCreateState.value = Resource.Loading()
            try {
                if (taskId == null) {
                    createNewTask(context, popUpScreen, onTaskCreated)
                } else {
                    editExistingTask(context, taskId, popUpScreen, onTaskCreated)
                }
                _taskEditCreateState.value = Resource.Success(Unit)
                SnackbarManager.showMessage("Task successfully saved")
            } catch (e: Exception) {
                _taskEditCreateState.value = Resource.Error(e.message ?: "Unknown error")
                SnackbarManager.showMessage("Unknown error")
            } finally {
                _taskEditCreateState.value = Resource.Empty()
            }
        }
    }

//    fun onDeleteImageClick() {
//        viewModelScope.launch {
//            task.value.imageUri?.let { imageUrl ->
//                val path = imageUrl.substringAfter("task_images/")
//                firebaseService.deleteImage(path)
//                task.value = task.value.copy(imageUri = null)
//                imageUri.value = null
//            }
//            imageUri.value = null
//        }
//    }
    fun onDeleteImageClick() {
        viewModelScope.launch {
            task.value.imageUri?.let { imageUrl ->
                val path = imageUrl
                    .substringAfter("example-f27a3.appspot.com/o/")
                    .substringBefore("?")
                    .replace("%2F", "/")

                Log.d("onDeleteImageClick", "imageUrl: $imageUrl")
                Log.d("onDeleteImageClick", "path: $path")

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
        alertTimeDisplay.value = "1 hour in advance"
        onLocationReset()
    }

    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }

    private fun alertTimeToMillis(option: String): Long {
        return when (option) {
            "5 minutes in advance" -> 5 * 60 * 1000L
            "10 minutes in advance" -> 10 * 60 * 1000L
            "15 minutes in advance" -> 15 * 60 * 1000L
            "30 minutes in advance" -> 30 * 60 * 1000L
            "1 hour in advance" -> 60 * 60 * 1000L
            "2 hours in advance" -> 2 * 60 * 60 * 1000L
            "3 hours in advance" -> 3 * 60 * 60 * 1000L
            "1 day in advance" -> 24 * 60 * 60 * 1000L
            else -> 0L
        }
    }

    private fun alertTimeToText(option: Long) {
        when (option) {
            5 * 60 * 1000L -> alertTimeDisplay.value = "5 minutes in advance"
            10 * 60 * 1000L -> alertTimeDisplay.value = "10 minutes in advance"
            15 * 60 * 1000L -> alertTimeDisplay.value = "15 minutes in advance"
            30 * 60 * 1000L -> alertTimeDisplay.value = "30 minutes in advance"
            60 * 60 * 1000L -> alertTimeDisplay.value = "1 hour in advance"
            2 * 60 * 60 * 1000L -> alertTimeDisplay.value = "2 hours in advance"
            3 * 60 * 60 * 1000L -> alertTimeDisplay.value = "3 hours in advance"
            24 * 60 * 60 * 1000L -> alertTimeDisplay.value = "1 day in advance"
            else -> alertTimeDisplay.value = "Not set"
        }
    }


    private fun generateUniqueNotificationId(): Int {
        return LocalDateTime.now().hashCode()
    }

}