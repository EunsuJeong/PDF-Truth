package com.pdftruth.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDocumentDao {

    @Query("SELECT * FROM recent_documents ORDER BY lastOpenedAt DESC")
    fun observeRecentDocuments(): Flow<List<RecentDocumentEntity>>

    @Upsert
    suspend fun upsert(entity: RecentDocumentEntity)
}