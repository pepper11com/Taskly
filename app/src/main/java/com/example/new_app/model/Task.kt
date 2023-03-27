package com.example.new_app.model

import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val assignedTo: List<String> = emptyList(),
    val isCompleted: Boolean = false
)
