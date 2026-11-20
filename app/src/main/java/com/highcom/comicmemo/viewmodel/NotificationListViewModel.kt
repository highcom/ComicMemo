package com.highcom.comicmemo.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.highcom.comicmemo.datamodel.BookNotification
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * お知らせ通知一覧用ViewModel
 *
 * @property repository 巻数データのデータ操作用リポジトリ
 */
@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val repository: ComicMemoRepository
) : ViewModel() {

    /** お知らせ通知一覧LiveData */
    val notifications: LiveData<List<BookNotification>> = repository.notifications.asLiveData()

    init {
        cleanExpiredNotifications()
    }

    /**
     * 期限切れ（過去日時）の通知データをDBから削除する
     */
    fun cleanExpiredNotifications() {
        viewModelScope.launch {
            repository.deleteExpiredNotifications(System.currentTimeMillis())
        }
    }

    /**
     * お知らせ通知データの削除およびアラームのキャンセル
     *
     * @param id 通知ID
     * @param context コンテキスト
     */
    fun deleteNotification(id: Long, context: Context) {
        viewModelScope.launch {
            NotificationScheduler.cancelNotification(context, id)
            repository.deleteNotification(id)
        }
    }
}
