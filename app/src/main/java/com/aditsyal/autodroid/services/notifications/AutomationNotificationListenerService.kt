package com.aditsyal.autodroid.services.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aditsyal.autodroid.automation.trigger.providers.AppEventTriggerProvider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AutomationNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var appEventTriggerProvider: AppEventTriggerProvider

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification ?: return
            
            val extras = notification.extras
            val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            
            val message = if (bigText.isNotEmpty()) bigText else text
            
            Timber.d("Notification posted from: $packageName, title: $title, text: $message")
            
            appEventTriggerProvider.onNotificationReceived(packageName, title, message)
        } catch (e: Exception) {
            Timber.e(e, "Error processing notification posted event")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification ?: return
            
            val extras = notification.extras
            val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
            
            Timber.d("Notification removed from: $packageName, title: $title")
        } catch (e: Exception) {
            Timber.e(e, "Error processing notification removed event")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("NotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.d("NotificationListenerService disconnected")
    }
}
