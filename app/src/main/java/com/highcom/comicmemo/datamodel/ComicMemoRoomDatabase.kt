package com.highcom.comicmemo.datamodel

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * SQLiteからRoomへのマイグレーション操作
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            // 新しいテーブルを一時テーブルとして構築
            db.execSQL("""
                CREATE TABLE comicdata_tmp(
                    id INTEGER PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL DEFAULT '',
                    author TEXT NOT NULL DEFAULT '',
                    number TEXT NOT NULL DEFAULT '1',
                    memo TEXT NOT NULL DEFAULT '',
                    inputdate TEXT NOT NULL DEFAULT '',
                    status INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            // 旧テーブルのデータを全て一時テーブルに追加
            db.execSQL("""
                INSERT INTO comicdata_tmp (id,title,author,number,memo,inputdate,status)
                SELECT id,title,author,number,memo,inputdate,status FROM comicdata
                """.trimIndent()
            )
            // 旧テーブルを削除
            db.execSQL("DROP TABLE comicdata")
            // 新テーブルをリネーム
            db.execSQL("ALTER TABLE comicdata_tmp RENAME TO comicdata")

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

/**
 * 新刊検索機能追加に伴うマイグレーション操作
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            // 著作者名の新しいテーブルを構築
            db.execSQL("""
                CREATE TABLE authorlist(
                    id INTEGER PRIMARY KEY NOT NULL,
                    author TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

/**
 * フリガナ・出版社・ISBNカラム追加によるマイグレーション
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            // 新しいテーブルを一時テーブルとして構築
            db.execSQL("""
                CREATE TABLE comicdata_tmp(
                    id INTEGER PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL DEFAULT '',
                    title_kana TEXT NOT NULL DEFAULT '',
                    author TEXT NOT NULL DEFAULT '',
                    publisher TEXT NOT NULL DEFAULT '',
                    isbn TEXT NOT NULL DEFAULT '',
                    number TEXT NOT NULL DEFAULT '1',
                    memo TEXT NOT NULL DEFAULT '',
                    inputdate TEXT NOT NULL DEFAULT '',
                    status INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            // 旧テーブルのデータを全て一時テーブルに追加
            db.execSQL("""
                INSERT INTO comicdata_tmp (id,title,author,number,memo,inputdate,status)
                SELECT id,title,author,number,memo,inputdate,status FROM comicdata
                """.trimIndent()
            )
            // 旧テーブルを削除
            db.execSQL("DROP TABLE comicdata")
            // 新テーブルをリネーム
            db.execSQL("ALTER TABLE comicdata_tmp RENAME TO comicdata")

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

/**
 * 巻数データのRoomデータベース
 */
@Database(entities = [Comic::class, Author::class],
    version = 5,
    exportSchema = false)
abstract class ComicMemoRoomDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
    abstract fun authorDao(): AuthorDao
}