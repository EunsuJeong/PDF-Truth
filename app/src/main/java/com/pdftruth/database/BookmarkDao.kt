package com.pdftruth.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE uri = :uri ORDER BY pageIndex ASC")
    fun observeBookmarks(uri: String): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE uri = :uri AND pageIndex = :pageIndex)")
    suspend fun isBookmarked(uri: String, pageIndex: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE uri = :uri AND pageIndex = :pageIndex")
    suspend fun deleteByUriAndPage(uri: String, pageIndex: Int)
}