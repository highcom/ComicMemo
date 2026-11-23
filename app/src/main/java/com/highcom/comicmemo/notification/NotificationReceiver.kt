package com.highcom.comicmemo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.highcom.comicmemo.R
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.ui.edit.ComicMemoActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * お知らせ通知の受信用BroadcastReceiver
 */
@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ComicMemoRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getLongExtra(NotificationScheduler.EXTRA_NOTIFICATION_ID, -1L)
        val title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE) ?: ""
        val author = intent.getStringExtra(NotificationScheduler.EXTRA_AUTHOR) ?: ""

        if (notificationId != -1L) {
            // 通知表示処理
            showNotification(context, notificationId, title, author)

            // 通知が完了したため、一覧（DB）から自動削除する
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.deleteNotification(notificationId)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * システム通知を表示する
     *
     * @param context コンテキスト
     * @param id 通知ID
     * @param title 書籍タイトル
     * @param author 著者名
     */
    private fun showNotification(context: Context, id: Long, title: String, author: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "book_notification_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, ComicMemoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (author.isNotEmpty()) {
            context.getString(R.string.notification_content_text_with_author, title, author)
        } else {
            context.getString(R.string.notification_content_text, title)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_content_title))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(id.toInt(), builder.build())
    }
}
