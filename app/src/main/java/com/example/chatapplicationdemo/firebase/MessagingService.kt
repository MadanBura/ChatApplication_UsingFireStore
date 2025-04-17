package com.example.chatapplicationdemo.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.activities.ChatActivity
import com.example.chatapplicationdemo.models.User
import com.example.chatapplicationdemo.utility.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message Received: ${message.notification?.body}")

        val user = User().apply {
            this.id = message.data[Constants.KEY_USER_ID] ?: ""
            this.name = message.data[Constants.KEY_NAME] ?: "Unknown User"
            this.token = message.data[Constants.KEY_FCM_TOKEN] ?: ""
        }

        val notificationId = Random.nextInt()
        val channelId = "chat_message"
        val intent = Intent(applicationContext, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.KEY_USER, user)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(user.name)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    message.data[Constants.KEY_MESSAGE] ?: "New message"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Chat Messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "This channel is used for chat notifications"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("FCM", "Notification Permission Not Granted")
                return
            }
        }

        // Show the notification
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())
    }
}
