package com.aditsyal.autodroid.domain.usecase.executors

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class CloseNotificationExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val action = config["action"]?.toString()?.lowercase() ?: "dismiss"
            val packageName = config["packageName"]?.toString()
            val notificationId = config["notificationId"]?.toString()?.toIntOrNull()
            val tag = config["tag"]?.toString()

            when (action) {
                "dismiss", "cancel" -> dismissNotification(packageName, notificationId, tag)
                "cancel_all", "clear_all" -> cancelAllNotifications(packageName)
                else -> throw IllegalArgumentException("Unknown notification action: $action")
            }

            Timber.i("Notification action executed: $action")
        }.onFailure { e ->
            Timber.e(e, "Notification action failed")
        }
    }

    private fun dismissNotification(packageName: String?, notificationId: Int?, tag: String?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    throw SecurityException("Notification policy access not granted. User must enable notification access for this app.")
                }
            }

            if (notificationId != null) {
                // Dismiss specific notification
                if (tag != null) {
                    notificationManager.cancel(tag, notificationId)
                    Timber.d("Dismissed notification: tag=$tag, id=$notificationId")
                } else {
                    notificationManager.cancel(notificationId)
                    Timber.d("Dismissed notification: id=$notificationId")
                }
            } else if (packageName != null) {
                // Dismiss all notifications from a specific package
                cancelAllNotifications(packageName)
            } else {
                throw IllegalArgumentException("Either notificationId or packageName must be specified")
            }

        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to dismiss notification: ${e.message}")
        }
    }

    private fun cancelAllNotifications(packageName: String?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    throw SecurityException("Notification policy access not granted")
                }
            }

            if (packageName != null) {
                // Cancel all notifications from specific package
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val activeNotifications = notificationManager.activeNotifications
                    val notificationsToCancel = activeNotifications.filter { it.packageName == packageName }

                    notificationsToCancel.forEach { notification ->
                        notificationManager.cancel(notification.tag, notification.id)
                        Timber.d("Cancelled notification from $packageName: ${notification.tag}:${notification.id}")
                    }

                    Timber.d("Cancelled ${notificationsToCancel.size} notifications from $packageName")
                } else {
                    Timber.w("Cancel by package not supported on Android versions below Lollipop")
                    throw UnsupportedOperationException("Package-specific notification cancellation requires Android 5.0+")
                }
            } else {
                // Cancel all notifications
                notificationManager.cancelAll()
                Timber.d("Cancelled all notifications")
            }

        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to cancel notifications: ${e.message}")
        }
    }

    // Utility methods
    fun getActiveNotifications(): List<NotificationInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationManager.activeNotifications.map { sbn ->
                    NotificationInfo(
                        packageName = sbn.packageName,
                        id = sbn.id,
                        tag = sbn.tag,
                        title = sbn.notification.extras.getString("android.title") ?: "",
                        text = sbn.notification.extras.getString("android.text") ?: ""
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get active notifications")
            emptyList()
        }
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // Not required for older versions
        }
    }

    fun getNotificationCount(packageName: String? = null): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val notifications = notificationManager.activeNotifications
                if (packageName != null) {
                    notifications.count { it.packageName == packageName }
                } else {
                    notifications.size
                }
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to count notifications")
            0
        }
    }

    data class NotificationInfo(
        val packageName: String,
        val id: Int,
        val tag: String?,
        val title: String,
        val text: String
    )
}