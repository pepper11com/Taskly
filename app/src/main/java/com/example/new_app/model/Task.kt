package com.example.new_app.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.new_app.screens.tasklist.TaskStatus
import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val assignedTo: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val status: TaskStatus = TaskStatus.ACTIVE,

    var imageUri: String? = null,
)
