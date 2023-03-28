package com.example.new_app.screens.createtask

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.new_app.TASK_DEFAULT_ID
import com.example.new_app.common.ext.idFromParameter
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import com.example.new_app.screens.TaskAppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class CreateTaskViewModel() : TaskAppViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"

    private val firebaseService: FirebaseService = FirebaseService()
    private val accountService: AccountService = AccountService()

    val task = mutableStateOf(Task())

    var bitmap by mutableStateOf<Bitmap?>(null)

    fun onTitleChange(newValue: String) {
        task.value = task.value.copy(title = newValue)
    }
    fun onDescriptionChange(newValue: String) {
        task.value = task.value.copy(description = newValue)
    }

    fun onIsCompletedChange(newValue: Boolean) {
        task.value = task.value.copy(isCompleted = newValue)
    }

    fun onImageChange(newValue: String) {
        task.value = task.value.copy(imageUri = newValue)
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

    fun initialize(taskId: String) {
        launchCatching {
            if (taskId != TASK_DEFAULT_ID) {
                task.value = firebaseService.getTask(taskId.idFromParameter()) ?: Task()
            }
        }
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
            try {
                val newTask = task.value.copy(
                    createdBy = accountService.currentUserId,
                    assignedTo = listOf(accountService.currentUserId),
                    isCompleted = false
                )
                firebaseService.save(newTask)
                popUpScreen()
            } catch (e: Exception) {
                // Handle the error
            }
        }
    }

    private fun Int.toClockPattern(): String {
        return if (this < 10) "0$this" else "$this"
    }
}
