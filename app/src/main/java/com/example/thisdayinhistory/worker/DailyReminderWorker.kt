package com.example.thisdayinhistory.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.thisdayinhistory.MainActivity
import com.example.thisdayinhistory.R
import com.example.thisdayinhistory.data.api.WikipediaApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val monthFormat = SimpleDateFormat("MM", Locale.US)
            val dayFormat = SimpleDateFormat("dd", Locale.US)
            val now = Date()
            val month = monthFormat.format(now)
            val day = dayFormat.format(now)

            val response = WikipediaApiService.instance.getOnThisDay("events", month, day)
            val randomEvent = response.events?.randomOrNull() ?: response.selected?.randomOrNull()

            val title = "On This Day in History"
            val text = if (randomEvent != null) {
                "${randomEvent.year}: ${randomEvent.text}"
            } else {
                "Check out what happened on this day in history!"
            }

            showNotification(title, text)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            showNotification("On This Day in History", "Discover historical events for today!")
            Result.success()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "daily_history_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily History Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for historical events on this day"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
