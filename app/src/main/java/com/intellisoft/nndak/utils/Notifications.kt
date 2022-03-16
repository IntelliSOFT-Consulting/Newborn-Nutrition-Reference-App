package com.intellisoft.nndak.utils

import android.content.Context

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