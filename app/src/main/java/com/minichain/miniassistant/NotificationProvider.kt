package com.minichain.miniassistant

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.minichain.miniassistant.MainService.Companion.CHANNEL_ID
import com.minichain.miniassistant.MainService.Companion.DISMISSAL_NOTIFICATION_ACTION_NAME

fun getNotification(context: Context) =
  NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_launcher_foreground)
    .setContentTitle("Mini Assistant is active")
    .setContentText("Mini Assistant debug text")
    .setContentIntent(getMainActivityPendingIntent(context))
    .setDeleteIntent(getDismissedNotificationIntent(context))
    .build()

private fun getMainActivityIntent(context: Context) =
  Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  }

private fun getMainActivityPendingIntent(context: Context) =
  PendingIntent.getActivity(context, 21389, getMainActivityIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

private fun getDismissedNotificationIntent(context: Context) =
  PendingIntent.getBroadcast(context, 2138, getDismissalNotificationIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

private fun getDismissalNotificationIntent(context: Context) =
  Intent(DISMISSAL_NOTIFICATION_ACTION_NAME).setPackage(context.packageName)
