package com.example.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.OutpostStateEntity
import com.example.domain.OutpostCalculator
import com.example.domain.ProductionSnapshot
import com.example.model.ActiveBuildingConstruction

object VaultNotificationHelper {

    const val CHANNEL_ID = "vault_city_offline_alerts"
    const val CHANNEL_NAME = "Ares Outpost Offline Alerts"

    const val ACTION_NOTIFY_ROOM_DONE = "com.example.notifications.ACTION_NOTIFY_ROOM_DONE"
    const val ACTION_NOTIFY_RESOURCES_MAX = "com.example.notifications.ACTION_NOTIFY_RESOURCES_MAX"
    const val ACTION_NOTIFY_POWER_OUT = "com.example.notifications.ACTION_NOTIFY_POWER_OUT"

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    const val NOTIF_ID_ROOM_DONE = 4101
    const val NOTIF_ID_RESOURCES_MAX = 4102
    const val NOTIF_ID_POWER_OUT = 4103

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when Outpost resources reach max capacity, power runs out, or a sector finishes upgrading while Ares Outpost is closed."
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return runCatching {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }.getOrDefault(true)
    }

    @SuppressLint("MissingPermission")
    fun postVaultAlertNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!hasNotificationPermission(context)) return
        ensureNotificationChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    fun notifyRoomUpgradeComplete(
        context: Context,
        roomName: String,
        targetLevel: Int
    ) {
        postVaultAlertNotification(
            context = context,
            notificationId = NOTIF_ID_ROOM_DONE,
            title = "Outpost Sector Upgrade Complete!",
            message = "$roomName has finished upgrading to Lv.$targetLevel! Open Ares Outpost to inspect your new efficiency."
        )
    }

    fun notifyResourcesAtMax(
        context: Context,
        resourceSummary: String,
        storageCap: Int
    ) {
        postVaultAlertNotification(
            context = context,
            notificationId = NOTIF_ID_RESOURCES_MAX,
            title = "Outpost Resources at Max Capacity!",
            message = "Stockpiles full ($resourceSummary • Cap $storageCap). Return to Ares Outpost to spend resources or upgrade your Logistics Depot!"
        )
    }

    fun notifyVaultOutOfPower(
        context: Context,
        reasonSummary: String
    ) {
        postVaultAlertNotification(
            context = context,
            notificationId = NOTIF_ID_POWER_OUT,
            title = "CRITICAL ALERT: Outpost Out of Power!",
            message = "$reasonSummary Automated sectors & turrets are offline—return to crank the Emergency Dynamo or refuel reactors!"
        )
    }

    /**
     * Schedules background phone notifications via AlarmManager when the game moves out of the foreground
     * so alerts fire when:
     * 1) A Vault Room finishes upgrading
     * 2) Resources reach max capacity
     * 3) The Vault runs out of power
     */
    fun scheduleOfflineNotifications(
        context: Context,
        state: OutpostStateEntity,
        production: ProductionSnapshot,
        activeConstruction: ActiveBuildingConstruction?
    ) {
        ensureNotificationChannel(context)
        cancelScheduledOfflineNotifications(context)

        val buildingId = activeConstruction?.buildingType?.id ?: state.activeConstructionBuildingId
        val targetLevel = activeConstruction?.targetLevel ?: state.activeConstructionTargetLevel
        val remainingSecs = activeConstruction?.remainingSeconds ?: state.activeConstructionRemainingSeconds

        val schedule = OutpostCalculator.computeOfflineNotificationSchedule(
            state = state,
            production = production,
            activeConstructionBuildingId = buildingId,
            activeConstructionTargetLevel = targetLevel,
            activeConstructionRemainingSeconds = remainingSecs
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nowElapsed = SystemClock.elapsedRealtime()

        schedule.secondsUntilRoomUpgradeComplete?.let { secs ->
            val pending = buildReceiverPendingIntent(
                context = context,
                action = ACTION_NOTIFY_ROOM_DONE,
                requestCode = NOTIF_ID_ROOM_DONE,
                notificationId = NOTIF_ID_ROOM_DONE,
                title = "Outpost Sector Upgrade Complete!",
                message = "${schedule.upgradingRoomTitle} finished upgrading to Lv.${schedule.upgradingRoomTargetLevel}! Return to Ares Outpost."
            )
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nowElapsed + secs * 1000L,
                pending
            )
        }

        schedule.secondsUntilResourcesAtMax?.let { secs ->
            val pending = buildReceiverPendingIntent(
                context = context,
                action = ACTION_NOTIFY_RESOURCES_MAX,
                requestCode = NOTIF_ID_RESOURCES_MAX,
                notificationId = NOTIF_ID_RESOURCES_MAX,
                title = "Outpost Resources at Max Capacity!",
                message = "${schedule.maxResourceSummary} reached maximum storage! Spend resources or upgrade your Outpost Depot."
            )
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nowElapsed + secs * 1000L,
                pending
            )
        }

        schedule.secondsUntilPowerOutage?.let { secs ->
            val pending = buildReceiverPendingIntent(
                context = context,
                action = ACTION_NOTIFY_POWER_OUT,
                requestCode = NOTIF_ID_POWER_OUT,
                notificationId = NOTIF_ID_POWER_OUT,
                title = "CRITICAL ALERT: Outpost Out of Power!",
                message = "${schedule.powerOutageReasonSummary} Return to Ares Outpost to crank the Emergency Dynamo or refuel reactors!"
            )
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                nowElapsed + secs * 1000L,
                pending
            )
        }
    }

    fun cancelScheduledOfflineNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        listOf(
            ACTION_NOTIFY_ROOM_DONE to NOTIF_ID_ROOM_DONE,
            ACTION_NOTIFY_RESOURCES_MAX to NOTIF_ID_RESOURCES_MAX,
            ACTION_NOTIFY_POWER_OUT to NOTIF_ID_POWER_OUT
        ).forEach { (action, reqCode) ->
            val intent = Intent(context, VaultNotificationReceiver::class.java).apply {
                this.action = action
            }
            val pending = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }
    }

    private fun buildReceiverPendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
        notificationId: Int,
        title: String,
        message: String
    ): PendingIntent {
        val intent = Intent(context, VaultNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
