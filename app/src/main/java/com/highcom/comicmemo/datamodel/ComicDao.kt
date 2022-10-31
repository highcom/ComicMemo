package com.highcom.comicmemo.datamodel

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    @Query("SELECT * FROM comicmemo WHERE status = :status ORDER BY id ASC")
    fun getComicByStatus(status: Long): Flow<List<Comic>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(comic: Comic)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(comic: Comic)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(comics: List<Comic>)

    @Query("DELETE FROM comicmemo WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM comicmemo")
    suspend fun deleteAll()
}