package com.highcom.comicmemo.datamodel

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 巻数データアクセスオブジェクト
 */
@Dao
interface ComicDao {
    @Query("SELECT * FROM comicdata WHERE status = :status ORDER BY id ASC")
    fun getComicByStatus(status: Long): Flow<List<Comic>>

    @Query("SELECT SUM(number) FROM comicdata WHERE status = :status")
    fun sumNumber(status: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(comic: Comic)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(comic: Comic)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(comics: List<Comic>)

    @Query("DELETE FROM comicdata WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM comicdata")
    suspend fun deleteAll()
}