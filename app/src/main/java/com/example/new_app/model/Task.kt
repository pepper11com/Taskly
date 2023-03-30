package com.example.new_app.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.new_app.screens.task.tasklist.TaskStatus
import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.util.Date

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
    val taskDate: Date = Date(),

    var imageUri: String? = null,
//    var imageBitmap: Bitmap? = null
)
