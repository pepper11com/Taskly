package com.example.new_app.screens.task.tasklist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.new_app.model.Task
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)


//TODO add more filters so the user can filter by date, priority, etc
@RequiresApi(Build.VERSION_CODES.O)
fun getFilteredTasks(tasks: List<Task>, status: TaskStatus): List<Task> {
    val sortedTasks = tasks.sortedWith(compareBy { task ->
        runCatching {
            LocalDate.parse(task.dueDate, dateFormat)
        }.getOrNull()
    })

    return sortedTasks.filter { task ->
        when (status) {
            TaskStatus.DELETED -> task.status == TaskStatus.DELETED
            TaskStatus.ACTIVE -> task.status == TaskStatus.ACTIVE
            TaskStatus.COMPLETED -> task.status == TaskStatus.COMPLETED
            else -> false
        }
    }
}

fun onSelectAllTasks(selectedIndex: Int, selectedTasks: SnapshotStateList<Task>, taskList: List<Task>) {

    val status = when (selectedIndex) {
        0 -> TaskStatus.DELETED
        2 -> TaskStatus.COMPLETED
        else -> TaskStatus.ACTIVE
    }

    val tasksToSelect = taskList.filter { it.status == status }

    tasksToSelect.forEach { task ->
        if (task !in selectedTasks) {
            selectedTasks.add(task)
        }
    }
}


fun getDueDateAndTime(task: Task): String {
    val stringBuilder = StringBuilder("")

    if (task.dueDate.orEmpty().isNotBlank()) {
        stringBuilder.append(task.dueDate)
        stringBuilder.append(" ")
    }

    if (task.dueTime.orEmpty().isNotBlank()) {
        stringBuilder.append("at ")
        stringBuilder.append(task.dueTime)
    }

    if (stringBuilder.isEmpty()) {
        stringBuilder.append("No due date and time set")
    }

    return stringBuilder.toString()
}
