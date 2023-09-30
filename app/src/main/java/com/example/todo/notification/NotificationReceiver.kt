package com.example.todo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todo.R

const val CHANNEL_ID = "TODO"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val contentText = intent?.extras!!.getString("contentText")
        val UID = intent.extras!!.getInt("TaskID")

        val notification = NotificationCompat.Builder(context!! , CHANNEL_ID )
            .setContentTitle("Todo")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_important)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        notificationManager.notify(UID , notification)

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

    }
}