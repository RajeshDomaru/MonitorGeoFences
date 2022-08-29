package com.span.monitorgeofences

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class NotificationHelper(base: Context) : ContextWrapper(base) {

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {

        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)

        notificationChannel.apply {

            enableLights(true)

            enableVibration(true)

            description = "This is the description of the channel."

            lightColor = Color.RED

            lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(notificationChannel)

    }

    fun sendHighPriorityNotification(title: String?, body: String?, activityName: Class<*>?) {

        val intent = Intent(this, activityName)

        var flag = PendingIntent.FLAG_UPDATE_CURRENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flag = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(this, 267, intent, flag)

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(
                    NotificationCompat.BigTextStyle().setSummaryText("summary")
                        .setBigContentTitle(title).bigText(body)
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this).notify(Random().nextInt(), notification)

    }

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannels()

    }

}