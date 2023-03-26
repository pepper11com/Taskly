package com.example.new_app.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val assignedTo: List<String> = emptyList(),
    val isCompleted: Boolean = false
)
