package com.example.new_app.common.sort

import com.example.new_app.TaskViewModel
import com.example.new_app.domain.model.Task
import com.example.new_app.screens.task.tasklist.TaskStatus
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

fun onSelectAllTasks(selectedIndex: Int, mainViewModel: TaskViewModel, taskList: List<Task>) {

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

    if (task.dueDate.isNotBlank()) {
        stringBuilder.append(task.dueDate)
        stringBuilder.append(" ")
    }

    if (task.dueTime.isNotBlank()) {
        stringBuilder.append("at ")
        stringBuilder.append(task.dueTime)
    }

    if (stringBuilder.isEmpty()) {
        stringBuilder.append("No due date and time set")
    }

    return stringBuilder.toString()
}

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
