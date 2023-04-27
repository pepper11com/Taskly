package com.example.new_app.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.new_app.screens.task.tasklist.TaskStatus
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

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
    var location: CustomLatLng? = null,
    var locationName: String? = null,
){
    fun dueDateToMillis(): Long? {
        if (dueDate.isEmpty() || dueTime.isEmpty()) {
            return null
        }

        val dateTimeFormat = "EEE, d MMM yyyy HH:mm"
        val dateTimeString = "$dueDate $dueTime"
        return try {
            val dateFormat = SimpleDateFormat(dateTimeFormat, Locale.ENGLISH)
            val date = dateFormat.parse(dateTimeString)
            date?.time
        } catch (e: Exception) {
            null
        }
    }
}


data class CustomLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)