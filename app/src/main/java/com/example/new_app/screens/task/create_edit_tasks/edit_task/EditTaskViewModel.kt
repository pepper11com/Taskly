package com.example.new_app.screens.task.create_edit_tasks.edit_task

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class EditTaskViewModel : ViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"

    private val firebaseService: FirebaseService = FirebaseService()
    private val accountService: AccountService = AccountService()

    val task = MutableStateFlow(Task()) // Replace Task() with your Task data class constructor

    var bitmap by mutableStateOf<Bitmap?>(null)


    suspend fun initialize(taskId: String) {
        val loadedTask = firebaseService.getTask(taskId)
        if (loadedTask != null) {
            task.value = loadedTask
        }
    }

    fun onTitleChange(newTitle: String) {
        task.value = task.value.copy(title = newTitle)
    }

    fun onDescriptionChange(newDescription: String) {
        task.value = task.value.copy(description = newDescription)
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
        }
    }

    fun onDoneClick(popUpScreen: () -> Unit) {
        viewModelScope.launch {
            firebaseService.updateTask(task.value)
            popUpScreen()
        }
    }

    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }
}