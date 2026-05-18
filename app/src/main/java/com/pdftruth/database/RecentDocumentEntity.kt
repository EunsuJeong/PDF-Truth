package com.pdftruth.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocumentEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val lastOpenedAt: Long,
)