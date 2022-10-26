package com.highcom.comicmemo

import android.app.Application
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.datamodel.ComicMemoRoomDatabase

class ComicMemoApplication : Application() {
    val database by lazy { ComicMemoRoomDatabase.getDatabase(this) }
    val repository by lazy { ComicMemoRepository(database.comicDao()) }
}