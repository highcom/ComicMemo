package com.highcom.comicmemo.datamodel

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 巻数データのデータ操作用リポジトリ
 *
 * @property comicDao 巻数データアクセスオブジェクト
 */
class ComicMemoRepository @Inject constructor(private val comicDao: ComicDao, private val authorDao: AuthorDao) {
    companion object {
        const val STATE_CONTINUE = 0L
        const val STATE_COMPLETE = 1L
    }

    val continueComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_CONTINUE)
    val completeComics: Flow<List<Comic>> = comicDao.getComicByStatus(STATE_COMPLETE)
    val authors: Flow<List<Author>> = authorDao.getAuthorList()

    fun getAuthorListSync(): List<Author> {
        return authorDao.getAuthorListSync()
    }

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
}