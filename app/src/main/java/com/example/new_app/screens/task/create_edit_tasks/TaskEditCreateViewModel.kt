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
import com.example.new_app.domain.model.CustomLatLng
import com.example.new_app.domain.model.Task
import com.example.new_app.domain.repository.AccountService
import com.example.new_app.domain.repository.FirebaseService
import com.example.new_app.domain.cancelTaskReminder
import com.example.new_app.domain.scheduleTaskReminder
import com.example.new_app.screens.task.tasklist.generateStaticMapUrl
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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

    // Format for the date to be displayed
    private val dateFormat = "EEE, d MMM yyyy"

    // State of the alert time to be displayed
    val alertTimeDisplay = mutableStateOf("1 hour in advance")

    // State of the task to be edited/created
    val task = mutableStateOf(Task())

    // State of the image URI for the task
    val imageUri = mutableStateOf<String?>(null)

    // Marker to indicate the location on a map
    private var marker: Marker? = null

    // State for managing the status of task editing/creating operations
    private val _taskEditCreateState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val taskEditCreateState: StateFlow<Resource<Unit>> get() = _taskEditCreateState

    // State of the location to be displayed
    val locationDisplay = mutableStateOf("")

    /**
     * Initializes the view model for editing an existing task.
     * Fetches the task details from Firebase and updates the task state.
     */
    fun initialize(taskId: String?) {
        viewModelScope.launch {
            taskId?.let {
                if (taskId.isNotEmpty()) {
                    task.value = firebaseService.getTask(taskId) ?: Task()

                    task.value.let {
                        locationDisplay.value = if (it.locationName == null) "No location set" else it.locationName!!
                    }

                    task.value.let {
                        imageUri.value = it.imageUri
                    }

                    alertTimeToText(task.value.alertMessageTimer)
                }
            }
        }
    }

    /**
     * Handles change in the alert time option.
     * Updates the alert time display and task state.
     */
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

    /**
     * Compresses the image selected for the task.
     * Runs on the IO dispatcher to avoid blocking the main thread.
     */
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

    /**
     * Uploads the compressed image to Firebase and saves the task.
     */
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

    /**
     * Creates a new task and saves it to Firebase.
     * If an image has been selected, it is uploaded as well.
     */
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
                SnackbarManager.showMessage("Task successfully created")
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

    /**
     * Edits an existing task and updates it in Firebase.
     * If an image has been selected, it is uploaded and the task is updated with the new image URI.
     */
    private suspend fun editExistingTask(
        context: Context,
        taskId: String,
        popUpScreen: () -> Unit,
        onTaskCreated: (String) -> Unit
    ) {

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
                    SnackbarManager.showMessage("Task successfully edited")
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
                    SnackbarManager.showMessage("Task successfully edited")
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

    /**
     * Handles the user clicking on the Done button.
     * If a task ID is provided, the existing task is edited; otherwise, a new task is created.
     */
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

            } catch (e: Exception) {
                _taskEditCreateState.value = Resource.Error(e.message ?: "Unknown error")
                SnackbarManager.showMessage("Unknown error")
            } finally {
                _taskEditCreateState.value = Resource.Empty()
            }
        }
    }

    /**
     * Deletes the image associated with the task from Firebase.
     * If the image exists, it is deleted and the task and image URI states are updated.
     */
    fun onDeleteImageClick() {
        viewModelScope.launch {
            val userId = accountService.currentUserId
            val taskId = task.value.id
            val path = "task_images/$userId/$taskId"

            task.value = task.value.copy(imageUri = null)
            imageUri.value = null

            if (firebaseService.doesImageExist(path)) {
                firebaseService.deleteImage(path)
            } else {
                // Handle the case when the image does not exist
                Log.d("onDeleteImageClick", "Image not found at path: $path")
            }
        }
    }

    /**
     * Resets the task state to its initial values.
     * This is typically called when the user wants to discard their changes and start over.
     */
    fun resetTask() {
        task.value = Task()
        imageUri.value = null
        alertTimeDisplay.value = "1 hour in advance"
        onLocationReset()
    }

    /**
     * Converts an Int to a clock pattern, padding with a leading zero if necessary.
     * This is used to format time values for display.
     */
    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }

    /**
     * Converts an alert time option to the corresponding duration in milliseconds.
     * This is used to set the task's alert time.
     */
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

    /**
     * Updates the alert time display state based on the alert time value.
     * This is used to display the selected alert time option to the user.
     */
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

    /**
     * Generates a unique notification ID for the task.
     * This is used to distinguish notifications for different tasks.
     */
    private fun generateUniqueNotificationId(): Int {
        return LocalDateTime.now().hashCode()
    }
}