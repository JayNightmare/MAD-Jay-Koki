package com.example.staysafe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.staysafe.MainActivity
import com.example.staysafe.R
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.UserWithContact
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StaySafeFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "StaySafeFirebaseMsg"
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

        suspend fun sendEmergencyNotification(
            context: Context,
            userId: Long,
            userName: String,
            currentActivity: Activity?,
            emergencyContacts: List<UserWithContact>
        ) {
            try {
                // Get the FCM token for the current device
                val token = Firebase.messaging.getToken().await()
                Log.d(TAG, "Current device FCM token: $token")

                // Send to all emergency contacts
                emergencyContacts.forEach { contact ->
                    try {
                        // Create the message data
                        val data = mapOf(
                            "type" to "emergency",
                            "userId" to userId.toString(),
                            "userName" to userName,
                            "activityId" to (currentActivity?.activityID?.toString() ?: ""),
                            "activityName" to (currentActivity?.activityName ?: ""),
                            "timestamp" to System.currentTimeMillis().toString()
                        )

                        // Create the notification payload
                        val notification = mapOf(
                            "title" to "Emergency Alert!",
                            "body" to "$userName needs immediate assistance!"
                        )

                        // Create the complete message payload
                        val message = mapOf(
                            "token" to contact.userID.toString(), // Using userID as token for now
                            "notification" to notification,
                            "data" to data
                        )

                        // Send the message using Firebase Admin SDK (via your backend)
                        // For now, we'll just log the message
                        Log.d(TAG, "Sending emergency notification to contact ${contact.userID}: $message")
                        
                        // TODO: Implement actual message sending through your backend
                        // This should be done through a secure backend service that has access to Firebase Admin SDK
                        // The backend should handle the actual sending of messages to specific FCM tokens
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending emergency notification to contact ${contact.userID}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending emergency notifications", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO: Send this token to your server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle emergency notifications
        if (remoteMessage.data["type"] == "emergency") {
            val userId = remoteMessage.data["userId"]?.toLongOrNull()
            val userName = remoteMessage.data["userName"]
            val activityId = remoteMessage.data["activityId"]?.toLongOrNull()

            if (userId != null && userName != null) {
                showEmergencyNotification(userId, userName, activityId)
            }
        }
    }

    private fun showEmergencyNotification(userId: Long, userName: String, activityId: Long?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", "emergency")
            putExtra("userId", userId)
            putExtra("userName", userName)
            activityId?.let {
                putExtra("activityId", it)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Emergency Alert!")
            .setContentText("$userName needs immediate assistance!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setLights(android.graphics.Color.RED, 3000, 3000)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
} 