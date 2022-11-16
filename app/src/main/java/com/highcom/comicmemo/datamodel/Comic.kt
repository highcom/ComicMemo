package com.highcom.comicmemo.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * 巻数データエンティティ
 *
 * @property id 巻数データID
 * @property title タイトル
 * @property author 著者名
 * @property number 巻数
 * @property memo メモ
 * @property inputdate 入力日付
 * @property status 続刊・完結の状態
 */
@Entity(tableName = "comicdata")
data class Comic(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "title", defaultValue = "") var title: String,
    @ColumnInfo(name = "author", defaultValue = "") var author: String,
    @ColumnInfo(name = "number", defaultValue = "1") var number: String,
    @ColumnInfo(name = "memo", defaultValue = "") var memo: String,
    @ColumnInfo(name = "inputdate", defaultValue = "") var inputdate: String,
    @ColumnInfo(name = "status", defaultValue = "0") var status: Long,
) : Serializable
