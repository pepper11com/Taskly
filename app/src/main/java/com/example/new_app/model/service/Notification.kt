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
import androidx.core.content.ContextCompat
import com.example.new_app.R
import okhttp3.OkHttpClient
import okhttp3.Request

class Notification (
    var context: Context,
    var title: String,
    var message: String,
){
    val channelID: String = "FCM100"
    val channelName: String = "Task Notification"
    val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationBuilder: NotificationCompat.Builder

    fun fireNotification(imageUrl: String?){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
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
            .addAction(android.R.drawable.ic_dialog_info, "Open", pendingIntent)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setLargeIcon(bitmap)
            .setAutoCancel(true)
            .setStyle(bigPictureStyle)
            .setColor(iconColor)
            .setContentIntent(pendingIntent)

        notificationManager.notify(100, notificationBuilder.build())
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

        val notification = Notification(applicationContext, title, message)
        notification.fireNotification(imageUrl)

        return Result.success()
    }
}

fun scheduleTaskReminder(taskId: String, title: String, message: String, dueDateMillis: Long, imageUrl: String?, context: Context) {

    val data = if (imageUrl != null){
        Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putString("imageUrl", imageUrl)
            .build()
    } else {
        Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()
    }

    val oneHourInMillis = TimeUnit.HOURS.toMillis(1)
    val timeUntilDueDate = dueDateMillis - System.currentTimeMillis() - oneHourInMillis

    // If the task is due within an hour, show the notification instantly
    val initialDelay = if (timeUntilDueDate <= oneHourInMillis) 0L else timeUntilDueDate

    val reminderRequest = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(taskId, ExistingWorkPolicy.REPLACE, reminderRequest)
}
