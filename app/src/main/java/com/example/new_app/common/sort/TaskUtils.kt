package com.example.new_app.common.sort

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.new_app.SharedViewModel
import com.example.new_app.model.Task
import com.example.new_app.screens.task.tasklist.TaskStatus
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

enum class TaskSortType {
    DATE_CREATED_ASC,
    DATE_CREATED_DESC,
    TITLE_ASC,
    TITLE_DESC,
    DUE_DATE_ASC,
    DUE_DATE_DESC,
    COLOR
}


@RequiresApi(Build.VERSION_CODES.O)
val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

@RequiresApi(Build.VERSION_CODES.O)
fun sortTasksByDueDate(tasks: List<Task>, ascending: Boolean): List<Task> {
    val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
    val sortedTasks = tasks.sortedWith(compareBy { task ->
        runCatching {
            LocalDate.parse(task.dueDate, dateFormat)
        }.getOrNull()
    })
    return if (ascending) sortedTasks else sortedTasks.reversed()
}

fun sortTasksByColor(tasks: List<Task>, ascending: Boolean): List<Task> {
    val sortedTasks = tasks.sortedBy { it.color }
    return if (ascending) sortedTasks else sortedTasks.reversed()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getFilteredTasks(tasks: List<Task>, status: TaskStatus, sortType: TaskSortType): List<Task> {
    val sortedTasks = sortTasks(tasks, sortType)

    return sortedTasks.filter { task ->
        when (status) {
            TaskStatus.DELETED -> task.status == TaskStatus.DELETED
            TaskStatus.ACTIVE -> task.status == TaskStatus.ACTIVE
            TaskStatus.COMPLETED -> task.status == TaskStatus.COMPLETED
            else -> false
        }
    }
}

//fun onSelectAllTasks(selectedIndex: Int, selectedTasks: SnapshotStateList<Task>, taskList: List<Task>) {
//
//    val status = when (selectedIndex) {
//        0 -> TaskStatus.DELETED
//        2 -> TaskStatus.COMPLETED
//        else -> TaskStatus.ACTIVE
//    }
//
//    val tasksToSelect = taskList.filter { it.status == status }
//
//    tasksToSelect.forEach { task ->
//        if (task !in selectedTasks) {
//            selectedTasks.add(task)
//        }
//    }
//}

fun onSelectAllTasks(selectedIndex: Int, mainViewModel: SharedViewModel, taskList: List<Task>) {

    val status = when (selectedIndex) {
        0 -> TaskStatus.DELETED
        2 -> TaskStatus.COMPLETED
        else -> TaskStatus.ACTIVE
    }

    val tasksToSelect = taskList.filter { it.status == status }

    tasksToSelect.forEach { task ->
        mainViewModel.onTaskSelection(task.id, true)
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

@RequiresApi(Build.VERSION_CODES.O)
fun sortTasks(tasks: List<Task>, sortType: TaskSortType): List<Task> {
    return when (sortType) {
        TaskSortType.DATE_CREATED_ASC -> tasks.sortedBy { it.taskDate }
        TaskSortType.DATE_CREATED_DESC -> tasks.sortedByDescending { it.taskDate }
        TaskSortType.TITLE_ASC -> tasks.sortedBy { it.title }
        TaskSortType.TITLE_DESC -> tasks.sortedByDescending { it.title }
        TaskSortType.DUE_DATE_ASC -> sortTasksByDueDate(tasks, true)
        TaskSortType.DUE_DATE_DESC -> sortTasksByDueDate(tasks, false)
        TaskSortType.COLOR -> sortTasksByColor(tasks, true)
    }
}
