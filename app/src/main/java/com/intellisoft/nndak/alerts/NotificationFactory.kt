package com.intellisoft.nndak.alerts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.models.SimpleNotification

class NotificationFactory(context: Context) {
    private var context: Context
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "com.intellisoft.nndak.notifications"
    private val description = "Notification"

    init {
        this.context = context
    }

    fun displayNotification(notification: SimpleNotification) {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)

        val pIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setContentTitle(notification.title)
                .setContentText(notification.content)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_stat_onesignal_default
                    )
                )
                .setContentIntent(pIntent)
        } else {

            builder = Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setContentTitle(notification.title)
                .setContentText(notification.content)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_stat_onesignal_default
                    )
                )
                .setContentIntent(pIntent)
        }
        notificationManager.notify(1234, builder.build())
    }
}

