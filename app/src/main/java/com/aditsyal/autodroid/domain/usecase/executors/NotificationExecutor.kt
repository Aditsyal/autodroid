package com.aditsyal.autodroid.domain.usecase.executors

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aditsyal.autodroid.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class NotificationExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    companion object {
        private const val ACTION_CHANNEL_ID = "action_channel"
        private const val ACTION_CHANNEL_NAME = "Automation Actions"
    }

    init {
        createNotificationChannel()
    }

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val title = config["title"]?.toString() ?: "Automation Executed"
            val message = config["message"]?.toString() ?: "Action completed successfully"
            val channelId = config["channelId"]?.toString() ?: ACTION_CHANNEL_ID

            // Check permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Timber.w("Missing POST_NOTIFICATIONS permission - notification will not be shown")
                    // Don't throw exception for missing notification permission - it's not critical
                    return@runCatching
                }
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ideally use a proper notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
            Timber.i("Notification shown: $title - $message")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ACTION_CHANNEL_ID,
                ACTION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channels for automation action notifications"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}