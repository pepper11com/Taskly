package com.example.new_app.screens.task.tasklist

import com.example.new_app.domain.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

