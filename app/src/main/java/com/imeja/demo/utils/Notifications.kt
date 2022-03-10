package com.imeja.demo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getSystemService
import com.imeja.demo.FhirApplication
import com.imeja.demo.MainActivity
import com.imeja.demo.R

object Notifications {

    fun notify(context: Context, title: String?, message: String?) {
        /*   val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_fire_engine)
           val mBuilder = NotificationCompat.Builder(context)
               .setSmallIcon(R.mipmap.ic_fire_engine)
               .setLargeIcon(bitmap)
               .setContentTitle(title)
               .setContentText(message)
           val resultIntent = Intent(context, MainActivity::class.java)
           val stackBuilder = TaskStackBuilder.create(context)
           stackBuilder.addParentStack(MainActivity::class.java)
           stackBuilder.addNextIntent(resultIntent)
           val resultPendingIntent = stackBuilder.getPendingIntent(
               0,
               PendingIntent.FLAG_UPDATE_CURRENT
           )
           mBuilder.setContentIntent(resultPendingIntent)
           mBuilder.setAutoCancel(true)
           val mNotificationManager = context
               .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
           mNotificationManager.notify(0, mBuilder.build())*/
        

}
}