package com.highcom.comicmemo.datamodel

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ComicMemoRepository(private val comicDao: ComicDao) {
    companion object {
        const val STATE_CONTINUE = 0L
        const val STATE_COMPLETE = 1L
    }

    val continueComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_CONTINUE)
    val completeComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_COMPLETE)

    @WorkerThread
    suspend fun insert(comic: Comic) {
        comicDao.insert(comic)
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
}