package com.bluebellcspl.maarevacommoditytradingapp.notificationHelper

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMNotificationService : FirebaseMessagingService() {
    private var NotificationId = 123
    var data: Map<String, String>? = null
    val TAG = "FCMNotificationService???"
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived FROM : ${message.from}")
        
        val data = message.data
        Log.d(TAG, "onMessageReceived: DATA : $data")
        if (message.notification!=null)
        {
            showNotification(message.notification!!)
        }
    }

    private fun showNotification(message: RemoteMessage.Notification) {

        // Show notification using the Firebase Notifications API

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_logo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)

        notificationManager.notify(NotificationId, notificationBuilder.build())
        NotificationId++

    }
}
