package com.highcom.comicmemo.datamodel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * お知らせ通知アクセスオブジェクト
 */
@Dao
interface NotificationDao {
    @Query("SELECT * FROM book_notification ORDER BY notifyDateTime ASC")
    fun getAllNotifications(): Flow<List<BookNotification>>

    @Query("SELECT * FROM book_notification ORDER BY notifyDateTime ASC")
    fun getAllNotificationsSync(): List<BookNotification>

    @Query("SELECT * FROM book_notification WHERE id = :id")
    suspend fun getNotificationById(id: Long): BookNotification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: BookNotification): Long

    @androidx.room.Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNotification(notification: BookNotification)

    @Query("DELETE FROM book_notification WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM book_notification WHERE id = :id")
    fun deleteNotificationSync(id: Long)

    @Query("DELETE FROM book_notification WHERE notifyDateTime < :currentMillis")
    suspend fun deleteExpiredNotifications(currentMillis: Long)

    @Query("SELECT COUNT(*) FROM book_notification")
    suspend fun getNotificationCount(): Int

    @Query("SELECT COUNT(*) FROM book_notification")
    fun getNotificationCountSync(): Int
}
