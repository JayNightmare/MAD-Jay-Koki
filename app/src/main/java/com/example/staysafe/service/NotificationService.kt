package com.example.staysafe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.staysafe.MainActivity
import com.example.staysafe.R
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.User
import com.example.staysafe.model.data.UserWithContact

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "StaySafeNotifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "StaySafe Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for StaySafe app"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showActivityStartedNotification(activity: Activity) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Activity Started")
            .setContentText("${activity.activityName} has started")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(activity.activityID.toInt(), notification)
    }

    // TODO: Fix Notification ->> Check if id is correct
    // TODO: When a contact is added, show a notification should be sent to the newly added contact
    // TODO: ->> If user 1 adds user 2 as a contact, user 2 should receive a notification
    fun showContactAddedNotification(user: UserWithContact) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Contact Added")
            .setContentText("${user.userFirstname} ${user.userLastname} has been added as a contact")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(user.userID.toInt(), notification)
    }

    fun showContactActivityStartedNotification(activity: Activity, contactName: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Contact Started Activity")
            .setContentText("$contactName has started ${activity.activityName}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(activity.activityID.toInt(), notification)
    }
} 