package com.highcom.comicmemo.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comicmemo")
data class Comic(
    @PrimaryKey
    @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "title", defaultValue = "") var title: String,
    @ColumnInfo(name = "author", defaultValue = "") var author: String,
    @ColumnInfo(name = "number", defaultValue = "1") var number: String,
    @ColumnInfo(name = "memo", defaultValue = "") var memo: String,
    @ColumnInfo(name = "inputdate", defaultValue = "") var inputdate: String,
    @ColumnInfo(name = "status", defaultValue = "0") var status: Long,
)
