package com.example.new_app.screens.createtask

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTaskCreated: Boolean = false
)

