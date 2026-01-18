package com.example.expensetracker.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.expensetracker.R
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.random.Random

class NotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "transaction_channel"
        const val NOTIFICATION_ID = 1
    }

    init {
        Log.d("NotificationService", "init")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        Log.d("NotificationService", "create channel")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Transactions"
            val descriptionText = "Notifications for new transactions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewTransactionNotification(title: String, amount: Double, income: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "no permissions")
                return
            }
        }

        val notificationId = Random.nextInt(1000, 9999)

        val contentText = if (income) {
            "$title: Tranzactie de +$amount RON a fost adaugata."
        } else {
            "$title: Tranzactie de -$amount RON a fost adaugata."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tranzactie noua")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        Log.d("NotificationService", "sending notif #$notificationId: $contentText")

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}