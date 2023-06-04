package com.example.new_app.model.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.new_app.MainActivity
import java.util.concurrent.TimeUnit
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.new_app.R
import okhttp3.OkHttpClient
import okhttp3.Request

class Notification (
    var context: Context,
    var title: String,
    var message: String,
){
    private val channelID: String = "FCM100"
    private val channelName: String = "Task Notification"
    private val groupKey = "com.example.taskly.NOTIFICATION_GROUP"
    private val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationBuilder: NotificationCompat.Builder

    fun fireNotification(imageUrl: String?, notificationId: Int){
        notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val bitmap = imageUrl?.let { url ->
            downloadImage(url)
        }

        val bigPictureStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(bitmap)
        val iconColor = ContextCompat.getColor(context, R.color.bright_orange)

        notificationBuilder = NotificationCompat.Builder(context, channelID)
            .setContentTitle(title)
            .addAction(R.drawable.ic_launcher, "Open", pendingIntent)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(bitmap)
            .setAutoCancel(true)
            .setStyle(bigPictureStyle)
            .setColor(iconColor)
            .setGroup(groupKey)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun downloadImage(url: String): Bitmap? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.let { BitmapFactory.decodeStream(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}

class TaskReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Task Reminder"
        val message = inputData.getString("message") ?: "A task is due!"
        val imageUrl = inputData.getString("imageUrl")

        val notificationId = inputData.getInt("notificationId", 100)

        val notification = Notification(applicationContext, title, message)
        notification.fireNotification(imageUrl, notificationId)

        return Result.success()
    }
}

fun scheduleTaskReminder(taskId: String, title: String, message: String, dueDateMillis: Long, imageUrl: String?, alertMessageTimer: Long?, notificationId: Int, context: Context) {

    val data = if (imageUrl != null) {
        Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putString("imageUrl", imageUrl)
            .putInt("notificationId", notificationId)
            .build()
    } else {
        Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putInt("notificationId", notificationId)
            .build()
    }

    val oneHourInMillis = TimeUnit.HOURS.toMillis(1)

    val initialDelay = if (alertMessageTimer != null) {
        dueDateMillis - System.currentTimeMillis() - alertMessageTimer
    } else {
        dueDateMillis - System.currentTimeMillis() - oneHourInMillis
    }

    val reminderRequest = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .build()

    Log.d("TaskReminder", "scheduleTaskReminder: $taskId")

    WorkManager.getInstance(context).enqueueUniqueWork(taskId, ExistingWorkPolicy.REPLACE, reminderRequest)
}

fun cancelTaskReminder(taskId: String, context: Context) {
    Log.d("TaskReminder", "cancelTaskReminder: $taskId")
    WorkManager.getInstance(context).cancelUniqueWork(taskId)
}


