package com.highcom.comicmemo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.highcom.comicmemo.datamodel.BookNotification

/**
 * お知らせ通知のスケジュール登録・解除用ヘルパークラス
 */
object NotificationScheduler {
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_AUTHOR = "extra_author"

    /**
     * お知らせ通知のアラームをスケジュール登録する
     *
     * @param context コンテキスト
     * @param notification お知らせ通知データ
     */
    fun scheduleNotification(context: Context, notification: BookNotification) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
            putExtra(EXTRA_TITLE, notification.title)
            putExtra(EXTRA_AUTHOR, notification.author)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id.toInt(),
            intent,
            flags
        )

        val triggerAtMillis = notification.notifyDateTime
        if (triggerAtMillis <= System.currentTimeMillis()) {
            // 過去日時の場合は即時発火
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * お知らせ通知のアラームをキャンセルする
     *
     * @param context コンテキスト
     * @param notificationId 通知ID
     */
    fun cancelNotification(context: Context, notificationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java)
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            intent,
            flags
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
