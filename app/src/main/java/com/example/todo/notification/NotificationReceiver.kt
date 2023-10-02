package com.example.todo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.todo.R
import com.example.todo.ui.activities.MainActivity


const val CHANNEL_ID = "TODO"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationReceiver","notification received")

        val contentText = intent?.extras!!.getString("contentText")
        val UID = intent.extras!!.getInt("TaskID")

        val clickIntent = Intent(context, MainActivity::class.java)
        clickIntent.putExtra("fragmentToOpen", "Important Tasks")

        val clickedPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(clickIntent)
            getPendingIntent(UID, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = NotificationCompat.Builder(context!!, CHANNEL_ID )
            .setContentTitle("Todo")
            .setContentText(contentText)
            .setContentIntent(clickedPendingIntent)
            .setAutoCancel(true)
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