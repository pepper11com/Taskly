package com.example.new_app.screens.task.create_edit_tasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.new_app.R
import com.example.new_app.TASK_DEFAULT_ID
import com.example.new_app.common.ext.idFromParameter
import com.example.new_app.model.CustomLatLng
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.screens.TaskAppViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class TaskEditCreateViewModel : TaskAppViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"

    private val firebaseService: FirebaseService = FirebaseService()
    private val accountService: AccountService = AccountService()

    val task = mutableStateOf(Task())
    val imageUri = mutableStateOf<String?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)
    var marker: Marker? = null

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

    fun onImageChange(newValue: String, context: Context, taskId: String, userId: String) {
        viewModelScope.launch {
            val localImagePath = accountService.saveImageToInternalStorage(
                context,
                Uri.parse(newValue),
                userId,
                taskId
            )
            task.value = task.value.copy(imageUri = localImagePath)
            imageUri.value = localImagePath

            bitmap = if (localImagePath.isNotEmpty()) {
                BitmapFactory.decodeFile(localImagePath)
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.baseline_account_box_24
                )
            }
        }
    }

    fun onDoneClick(taskId: String?, popUpScreen: () -> Unit) {
        viewModelScope.launch {
            if (taskId == null) {
                val newTask = task.value.copy(
                    createdBy = accountService.currentUserId,
                    assignedTo = listOf(accountService.currentUserId),
                    isCompleted = false
                )
                firebaseService.save(newTask)
            } else {
                firebaseService.updateTask(task.value)
            }
            popUpScreen()
        }
    }

    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }
}