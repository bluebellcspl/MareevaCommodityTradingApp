package com.bluebellcspl.maarevacommoditytradingapp.notificationHelper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluebellcspl.maarevacommoditytradingapp.HomeActivity
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMNotificationService : FirebaseMessagingService() {
    private var NotificationId = 123
    var data: Map<String, String>? = null
    val TAG = "FCMNotificationService???"
    var notificationManager:NotificationManager?=null
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onCreate() {
        super.onCreate()
        DatabaseManager.initializeInstance(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived FROM : ${message.from}")
        if (message.data.isNotEmpty())
        {
            val dataPayload = message.data
            Log.d(TAG, "onMessageReceived: NOTIFICATION_DATA : $dataPayload")
            if (dataPayload["NotificationType"].equals("Chat"))
            {
                insertTempChatNotification(message)
            }
        }
        if (message.notification != null) {
//            showNotification(message.notification!!)
            sendNotification(message.notification!!)
            sendNotificationReceivedBroadcast()
            insertTempNotification(message.notification!!)

        }
    }

    private fun sendNotification(message: RemoteMessage.Notification) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            getIntentFromNotification(""),
            PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "com.bluebellcspl.maarevacommoditytradingapp"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(message.title)
                .setContentText(message.body)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_logo))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(NotificationId, notificationBuilder.build())
        NotificationId++
    }

    private fun getIntentFromNotification(pushType: String): Intent? {
        var intent: Intent? = null
        if (PrefUtil.getBoolean(PrefUtil.KEY_LOGGEDIN, false)) {
            intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        } else {
            intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return intent
    }

    private fun insertTempNotification(message: RemoteMessage.Notification){
        try {
            val list = ContentValues()
            list.put("TmpNotificationId",NotificationId)
            list.put("Title",message.title)
            list.put("FullMsg",message.body)
            list.put("Cdate",DateUtility().getyyyyMMdd())
            list.put("IsRead","false")

            DatabaseManager.commonInsert(list,Constants.TBL_TempNotificationMaster)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "insertTempNotification: ${e.message}")
        }
    }

    private fun insertTempChatNotification(message: RemoteMessage){
        try {
            val list = ContentValues()
            list.put("TmpChatNotificationId",NotificationId)
            list.put("RegisterId",message.data["RegisterId"])
            list.put("Cdate",DateUtility().getyyyyMMdd())
            list.put("IsRead","false")

            DatabaseManager.commonInsert(list,Constants.TBL_TempChatNotification)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "insertTempChatNotification: ${e.message}")
        }
    }

    private fun sendNotificationReceivedBroadcast() {
        val intent = Intent("ACTION_NOTIFICATION_RECEIVED")
        this.sendBroadcast(intent)
    }

}
