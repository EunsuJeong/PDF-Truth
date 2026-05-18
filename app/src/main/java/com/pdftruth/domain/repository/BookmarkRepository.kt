package com.pdftruth.domain.repository

import com.pdftruth.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeBookmarks(uri: String): Flow<List<Bookmark>>
    suspend fun isBookmarked(uri: String, pageIndex: Int): Boolean
    suspend fun setBookmarked(uri: String, pageIndex: Int, bookmarked: Boolean)
}