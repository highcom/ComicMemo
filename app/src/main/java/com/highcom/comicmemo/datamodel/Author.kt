package com.highcom.comicmemo.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 著作者名エンティティ
 *
 * @property id 著作者名リストID
 * @property author 著作者名
 */
@Entity(tableName = "authorlist")
data class Author(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "author", defaultValue = "") var author: String,
)