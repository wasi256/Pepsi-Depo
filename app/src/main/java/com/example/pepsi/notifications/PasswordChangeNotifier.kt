package com.example.pepsi.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val CHANNEL_ID = "password_change_requests"
private const val NOTIFICATION_ID = 1001

/**
 * Simulates the system administrator being alerted that a manager wants a new
 * password assigned. No-ops quietly if notification permission was never granted.
 */
fun notifySystemAdminOfPasswordChangeRequest(context: Context, managerName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Password Change Requests",
            NotificationManager.IMPORTANCE_HIGH,
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    val message = "$managerName wants to change their password. Please assign a new one."
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_lock)
        .setContentTitle("Password change request")
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    runCatching {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
