package com.pdftruth.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    @Query("SELECT * FROM reading_progress WHERE uri = :uri LIMIT 1")
    fun observeProgress(uri: String): Flow<ReadingProgressEntity?>

    @Upsert
    suspend fun upsert(entity: ReadingProgressEntity)
}