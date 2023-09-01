package com.highcom.comicmemo.datamodel

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 著作者名アクセスオブジェクト
 */
@Dao
interface AuthorDao {
    @Query("SELECT * FROM authorlist ORDER BY id ASC")
    fun getAuthorList(): Flow<List<Author>>

    @Query("SELECT * FROM authorlist ORDER BY id ASC")
    fun getAuthorListSync(): List<Author>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthor(author: Author)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAuthor(author: Author)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAuthors(authors: List<Author>)

    @Query("DELETE FROM authorlist WHERE id = :id")
    suspend fun deleteAuthor(id: Long)
}