package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VaultNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val notifId = intent.getIntExtra(
            VaultNotificationHelper.EXTRA_NOTIFICATION_ID,
            VaultNotificationHelper.NOTIF_ID_ROOM_DONE
        )
        val title = intent.getStringExtra(VaultNotificationHelper.EXTRA_TITLE)
            ?: "Vault City Alert"
        val message = intent.getStringExtra(VaultNotificationHelper.EXTRA_MESSAGE)
            ?: "Check on your underground Vault shelter!"

        VaultNotificationHelper.postVaultAlertNotification(
            context = context,
            notificationId = notifId,
            title = title,
            message = message
        )
    }
}
