package com.example.new_app.screens.task.create_edit_tasks.edit_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_app.model.Task
import com.example.new_app.model.service.AccountService
import com.example.new_app.model.service.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val accountService: AccountService
) : ViewModel() {

    private val dateFormat = "EEE, d MMM yyyy"

    val task = MutableStateFlow(Task())

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