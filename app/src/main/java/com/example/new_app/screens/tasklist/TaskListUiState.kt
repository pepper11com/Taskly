package com.example.new_app.screens.tasklist

import com.example.new_app.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

