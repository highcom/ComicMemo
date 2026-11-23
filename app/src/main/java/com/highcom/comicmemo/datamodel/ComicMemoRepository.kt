package com.highcom.comicmemo.datamodel

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 巻数データのデータ操作用リポジトリ
 *
 * @property comicDao 巻数データアクセスオブジェクト
 * @property authorDao 著作者名アクセスオブジェクト
 * @property notificationDao お知らせ通知アクセスオブジェクト
 */
class ComicMemoRepository @Inject constructor(
    private val comicDao: ComicDao,
    private val authorDao: AuthorDao,
    private val notificationDao: NotificationDao
) {
    companion object {
        const val STATE_CONTINUE = 0L
        const val STATE_COMPLETE = 1L
    }

    val continueComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_CONTINUE)
    val completeComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_COMPLETE)
    val authors: Flow<List<Author>> = authorDao.getAuthorList()
    val notifications: Flow<List<BookNotification>> = notificationDao.getAllNotifications()
    val sumContinueNumber: Flow<Long> = comicDao.sumNumber(STATE_CONTINUE)
    val sumCompleteNumber: Flow<Long> = comicDao.sumNumber(STATE_COMPLETE)

    fun getAuthorListSync(): List<Author> {
        return authorDao.getAuthorListSync()
    }

    fun getNotificationsSync(): List<BookNotification> {
        return notificationDao.getAllNotificationsSync()
    }

    @WorkerThread
    suspend fun insert(comic: Comic) {
        comicDao.insert(comic)
    }

    @WorkerThread
    suspend fun getCount(): Int {
        return comicDao.getCount()
    }

    @WorkerThread
    suspend fun update(comic: Comic) {
        comicDao.update(comic)
    }

    @WorkerThread
    suspend fun update(comics: List<Comic>) {
        comicDao.update(comics)
    }

    @WorkerThread
    suspend fun delete(id: Long) {
        comicDao.delete(id)
    }

    @WorkerThread
    suspend fun deleteAll() {
        comicDao.deleteAll()
    }

    @WorkerThread
    suspend fun insertAuthor(author: Author) {
        authorDao.insertAuthor(author)
    }

    @WorkerThread
    suspend fun updateAuthor(author: Author) {
        authorDao.updateAuthor(author)
    }

    @WorkerThread
    suspend fun updateAuthors(authors: List<Author>) {
        authorDao.updateAuthors(authors)
    }

    @WorkerThread
    suspend fun deleteAuthor(id: Long) {
        authorDao.deleteAuthor(id)
    }

    @WorkerThread
    suspend fun getAllComics(): List<Comic> {
        return comicDao.getAllComics()
    }

    @WorkerThread
    suspend fun insertNotification(notification: BookNotification): Long {
        return notificationDao.insertNotification(notification)
    }

    @WorkerThread
    suspend fun deleteNotification(id: Long) {
        notificationDao.deleteNotification(id)
    }

    fun deleteNotificationSync(id: Long) {
        notificationDao.deleteNotificationSync(id)
    }

    @WorkerThread
    suspend fun deleteExpiredNotifications(currentMillis: Long) {
        notificationDao.deleteExpiredNotifications(currentMillis)
    }

    @WorkerThread
    suspend fun getNotificationCount(): Int {
        return notificationDao.getNotificationCount()
    }

    fun getNotificationCountSync(): Int {
        return notificationDao.getNotificationCountSync()
    }
}