package com.example.staysafe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import com.example.staysafe.MainActivity
import com.example.staysafe.R
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.User
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.view.screens.EmergencyScreen
import com.example.staysafe.nav.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService internal constructor(
    private val context: Context,
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "StaySafeNotifications"
    private val panicChannelId = "PanicAlerts"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Regular notifications channel
            val regularChannel = NotificationChannel(
                channelId,
                "StaySafe Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Regular notifications for StaySafe app"
            }
            notificationManager.createNotificationChannel(regularChannel)

            // Panic alerts channel
            val panicChannel = NotificationChannel(
                panicChannelId,
                "Panic Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency panic alerts"
                enableVibration(true)
                enableLights(true)
                setSound(null, null) // Use system default sound
            }
            notificationManager.createNotificationChannel(panicChannel)
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

    fun showPanicAlertNotification(user: UserWithContact, emergencyContacts: List<UserWithContact>) {
        // Send Firebase notification to emergency contacts
        CoroutineScope(Dispatchers.IO).launch {
            StaySafeFirebaseMessagingService.sendEmergencyNotification(
                context = context,
                userId = user.userID,
                userName = "${user.userFirstname} ${user.userLastname}",
                currentActivity = null, // TODO: Get current activity if available
                emergencyContacts = emergencyContacts
            )
        }

        // Show local notification
        val notification = NotificationCompat.Builder(context, panicChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚨 PANIC ALERT")
            .setContentText("${user.userFirstname} ${user.userLastname} has triggered a panic alert!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibrate pattern
            .setLights(android.graphics.Color.RED, 3000, 3000) // Red light
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(user.userID.toInt(), notification)
    }

    fun showRouteDeviationAlert(activity: Activity) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Route Deviation Alert")
            .setContentText("You have deviated from your planned route for ${activity.activityName}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(activity.activityID.toInt(), notification)
    }

    fun showLowBatteryAlert() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Low Battery Alert")
            .setContentText("Your device battery is running low. Please charge your device.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(1000, notification) // Using a fixed ID for battery alerts
    }

    fun showNetworkAlert() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Network Alert")
            .setContentText("You have lost internet connection. Some safety features may be limited.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(1001, notification) // Using a fixed ID for network alerts
    }

    companion object {
        private const val CHANNEL_ID = "emergency_channel"
        private const val NOTIFICATION_ID = 1

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Emergency Alerts"
                val descriptionText = "Notifications for emergency situations"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showEmergencyNotification(
            context: Context,
            userId: Long,
            userName: String,
            currentActivity: Activity?,
        ) {
            // Create intent for the emergency screen
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("screen", "emergency")
                putExtra("userId", userId)
                putExtra("userName", userName)
                currentActivity?.let {
                    putExtra("activityId", it.activityID)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Emergency Alert!")
                .setContentText("$userName needs immediate assistance!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setLights(android.graphics.Color.RED, 3000, 3000)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        fun showActivityStatusNotification(
            context: Context,
            activityName: String,
            status: String
        ) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Activity Status Update")
                .setContentText("$activityName is now $status")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID + 1, notification)
        }

        fun showLocationUpdateNotification(
            context: Context,
            userName: String,
            latitude: Double,
            longitude: Double
        ) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Location Update")
                .setContentText("$userName's location has been updated")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID + 2, notification)
        }
    }
} 