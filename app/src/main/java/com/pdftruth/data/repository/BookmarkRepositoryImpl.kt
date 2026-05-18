package com.pdftruth.data.repository

import com.pdftruth.database.BookmarkDao
import com.pdftruth.database.BookmarkEntity
import com.pdftruth.domain.model.Bookmark
import com.pdftruth.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao,
) : BookmarkRepository {

    override fun observeBookmarks(uri: String): Flow<List<Bookmark>> {
        return bookmarkDao.observeBookmarks(uri).map { entities ->
            entities.map { entity ->
                Bookmark(
                    uri = entity.uri,
                    pageIndex = entity.pageIndex,
                    createdAt = entity.createdAt,
                )
            }
        }
    }

    override suspend fun isBookmarked(uri: String, pageIndex: Int): Boolean {
        return bookmarkDao.isBookmarked(uri, pageIndex)
    }

    override suspend fun setBookmarked(uri: String, pageIndex: Int, bookmarked: Boolean) {
        if (bookmarked) {
            bookmarkDao.insert(
                BookmarkEntity(
                    uri = uri,
                    pageIndex = pageIndex,
                    createdAt = System.currentTimeMillis(),
                )
            )
        } else {
            bookmarkDao.deleteByUriAndPage(uri, pageIndex)
        }
    }
}