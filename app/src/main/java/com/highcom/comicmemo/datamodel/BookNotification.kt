package com.highcom.comicmemo.datamodel

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * お知らせ通知データモデル
 *
 * @property id 通知ID（主キー）
 * @property title 書籍タイトル
 * @property author 著者名
 * @property publisher 出版社
 * @property isbn ISBN番号
 * @property imageUrl 画像URL
 * @property salesDate 発売日
 * @property notifyDateTime 通知予定日時（エポックミリ秒）
 */
@Entity(tableName = "book_notification")
data class BookNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val isbn: String = "",
    val imageUrl: String = "",
    val salesDate: String = "",
    val notifyDateTime: Long = 0L
)
