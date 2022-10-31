package com.highcom.comicmemo.datamodel

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Comic::class],
    version = 3,
    // TODO:手動でのマイグレーション方法
//    autoMigrations = [
//        AutoMigration (from = 2, to = 3)
//    ]
    exportSchema = false)
abstract class ComicMemoRoomDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao

    companion object {
        @Volatile
        private var INSTANCE: ComicMemoRoomDatabase? = null

        fun getDatabase(
            context: Context,
        ): ComicMemoRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ComicMemoRoomDatabase::class.java,
                    "comicmemo"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}