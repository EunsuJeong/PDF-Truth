package com.pdftruth.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdftruth.domain.model.ReadingProgress
import com.pdftruth.domain.model.RecentDocument
import com.pdftruth.domain.repository.BookmarkRepository
import com.pdftruth.domain.repository.DocumentRepository
import com.pdftruth.domain.repository.PdfSearchRepository
import com.pdftruth.domain.repository.ReaderPreferencesRepository
import com.pdftruth.domain.repository.ReadingProgressRepository
import com.pdftruth.domain.repository.RecentDocumentRepository
import com.pdftruth.util.DefaultDispatcherProvider
import com.pdftruth.util.PageBitmapLruCache
import com.pdftruth.util.ThumbnailBitmapLruCache
import com.pdftruth.viewer.PageRenderRequest
import com.pdftruth.viewer.PdfDocumentSession
import com.pdftruth.viewer.PdfDocumentSource
import com.pdftruth.viewer.engine.AndroidPdfRendererEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViewerViewModel(
    private val recentDocumentRepository: RecentDocumentRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val readerPreferencesRepository: ReaderPreferencesRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val pdfSearchRepository: PdfSearchRepository,
    private val documentRepository: DocumentRepository,
    context: Context,
) : ViewModel() {

    private val dispatcherProvider = DefaultDispatcherProvider
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Idle)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private val appContext = context.applicationContext
    private var engine: AndroidPdfRendererEngine? = null
    private var currentSession: PdfDocumentSession? = null
    private var currentDisplayName: String? = null
    private var pageStates: MutableList<PageUiState> = mutableListOf()
    private val bitmapCache = PageBitmapLruCache(5)
    private val thumbnailCache = ThumbnailBitmapLruCache(20)
    private var bookmarkObserveJob: Job? = null

    fun openPdf(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ViewerUiState.Error("PDF file is not selected.")
            return
        }
        val uriString = uri.toString()
        engine = AndroidPdfRendererEngine(appContext)
        _uiState.value = ViewerUiState.Loading

        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val document = documentRepository.getDocument(uriString)
                currentDisplayName = document.displayName

                val session = engine!!.openDocument(PdfDocumentSource(uriString))
                currentSession = session
                val pageCount = session.pageCount
                pageStates = MutableList(pageCount) { PageUiState.Loading }

                val restoredPage = readingProgressRepository.observeProgress(uriString)
                    .first()
                    ?.lastPageIndex
                    ?.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                    ?: 0

                _uiState.value = ViewerUiState.Success(
                    pageCount = pageCount,
                    pages = pageStates.toList(),
                    fileName = document.displayName,
                    documentUri = uriString,
                    currentPage = restoredPage,
                    bookmarkedPages = emptySet(),
                    thumbnails = buildInitialThumbnails(pageCount),
                )

                readerPreferencesRepository.saveLastOpenedDocumentUri(uriString)
                persistRecentDocument(uriString)
                persistReadingProgress(uriString, restoredPage)
                observeBookmarks(uriString)
                prefetchAround(restoredPage)
                prefetchThumbnailsAround(restoredPage)
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error("Failed to open PDF: ${e.localizedMessage}")
            }
        }
    }

    fun setCurrentPage(page: Int) {
        val state = _uiState.value
        if (state !is ViewerUiState.Success) return

        val target = page.coerceIn(0, (state.pageCount - 1).coerceAtLeast(0))
        if (target == state.currentPage) return

        _uiState.value = state.copy(currentPage = target)

        val uri = state.documentUri ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            persistReadingProgress(uri, target)
            persistRecentDocument(uri)
            prefetchAround(target)
            prefetchThumbnailsAround(target)
        }
    }

    fun toggleThumbnails() {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            _uiState.value = state.copy(showThumbnails = !state.showThumbnails)
        }
    }

    fun toggleCurrentPageBookmark() {
        val state = _uiState.value
        if (state !is ViewerUiState.Success) return

        val uri = state.documentUri ?: return
        val page = state.currentPage
        val bookmarked = state.bookmarkedPages.contains(page)

        viewModelScope.launch(dispatcherProvider.io) {
            bookmarkRepository.setBookmarked(uri, page, !bookmarked)
        }
    }

    fun updateSearchQuery(query: String) {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            _uiState.value = state.copy(searchQuery = query)
        }
    }

    fun executeSearch() {
        val state = _uiState.value
        if (state !is ViewerUiState.Success) return

        val query = state.searchQuery.trim()
        val uri = state.documentUri ?: return
        if (query.isEmpty()) {
            _uiState.value = state.copy(searchResults = emptyList(), searchNotice = "Enter a search query")
            return
        }

        _uiState.value = state.copy(isSearching = true, searchNotice = null)
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val outcome = pdfSearchRepository.search(
                    documentUri = uri,
                    query = query,
                    pageCount = state.pageCount,
                )

                val current = _uiState.value
                if (current is ViewerUiState.Success) {
                    when (outcome) {
                        is com.pdftruth.domain.model.PdfSearchOutcome.Success -> {
                            val results = outcome.results
                            val notice = if (results.isEmpty()) "No results found." else null
                            _uiState.value = current.copy(
                                isSearching = false,
                                searchNotice = notice,
                                searchResults = results.map {
                                    SearchResultUi(pageIndex = it.pageIndex, summary = it.summary)
                                },
                            )
                        }
                        is com.pdftruth.domain.model.PdfSearchOutcome.Empty -> {
                            _uiState.value = current.copy(
                                isSearching = false,
                                searchNotice = "No results found.",
                                searchResults = emptyList(),
                            )
                        }
                        is com.pdftruth.domain.model.PdfSearchOutcome.Unsupported -> {
                            _uiState.value = current.copy(
                                isSearching = false,
                                searchNotice = outcome.message,
                                searchResults = emptyList(),
                            )
                        }
                        is com.pdftruth.domain.model.PdfSearchOutcome.Failure -> {
                            _uiState.value = current.copy(
                                isSearching = false,
                                searchNotice = "Search failed: ${outcome.message}",
                                searchResults = emptyList(),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val current = _uiState.value
                if (current is ViewerUiState.Success) {
                    _uiState.value = current.copy(
                        isSearching = false,
                        searchNotice = "Search failed: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    fun openSearchResult(pageIndex: Int) {
        setCurrentPage(pageIndex)
    }

    fun openThumbnailPage(pageIndex: Int) {
        setCurrentPage(pageIndex)
    }

    private suspend fun prefetchAround(centerPage: Int) {
        val state = _uiState.value
        val session = currentSession ?: return
        if (state !is ViewerUiState.Success) return

        val start = (centerPage - 2).coerceAtLeast(0)
        val end = (centerPage + 2).coerceAtMost(state.pageCount - 1)
        for (index in start..end) {
            renderPageIfNeeded(index, state.pageCount, session)
        }
    }

    private suspend fun prefetchThumbnailsAround(centerPage: Int) {
        val state = _uiState.value
        val session = currentSession ?: return
        if (state !is ViewerUiState.Success) return

        val start = (centerPage - 4).coerceAtLeast(0)
        val end = (centerPage + 4).coerceAtMost(state.pageCount - 1)
        for (index in start..end) {
            renderThumbnailIfNeeded(index, state.pageCount, session)
        }
    }

    private suspend fun renderThumbnailIfNeeded(index: Int, pageCount: Int, session: PdfDocumentSession) {
        val cached = thumbnailCache[index]
        if (cached != null) {
            publishThumbnail(index, cached, pageCount)
            return
        }

        try {
            val bitmap = engine?.renderPage(
                session,
                PageRenderRequest(pageIndex = index, width = 220, height = 300),
            )?.bitmap
            if (bitmap != null) {
                thumbnailCache.put(index, bitmap)
                publishThumbnail(index, bitmap, pageCount)
            }
        } catch (_: Exception) {
            // 썸네일 실패는 본문 렌더링에 영향 주지 않음
        }
    }

    private suspend fun renderPageIfNeeded(index: Int, pageCount: Int, session: PdfDocumentSession) {
        val cached = bitmapCache[index]
        if (cached != null) {
            pageStates[index] = PageUiState.BitmapReady(cached)
            publish(pageCount)
            return
        }

        try {
            val bitmap = engine?.renderPage(
                session,
                PageRenderRequest(pageIndex = index, width = 1080, height = 1440),
            )?.bitmap

            if (bitmap != null) {
                bitmapCache.put(index, bitmap)
                pageStates[index] = PageUiState.BitmapReady(bitmap)
            } else {
                pageStates[index] = PageUiState.Error("Failed to render page")
            }
        } catch (e: Exception) {
            pageStates[index] = PageUiState.Error("Failed to render page: ${e.localizedMessage}")
        }

        publish(pageCount)
    }

    private fun publish(pageCount: Int) {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            _uiState.value = state.copy(
                pageCount = pageCount,
                pages = pageStates.toList(),
            )
        }
    }

    private fun publishThumbnail(index: Int, bitmap: Bitmap, pageCount: Int) {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            val updated = state.thumbnails.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(bitmap = bitmap)
                _uiState.value = state.copy(
                    pageCount = pageCount,
                    thumbnails = updated,
                )
            }
        }
    }

    private fun buildInitialThumbnails(pageCount: Int): List<ThumbnailUi> {
        return List(pageCount) { index ->
            ThumbnailUi(pageIndex = index, bitmap = null)
        }
    }

    private fun observeBookmarks(uri: String) {
        bookmarkObserveJob?.cancel()
        bookmarkObserveJob = viewModelScope.launch(dispatcherProvider.io) {
            bookmarkRepository.observeBookmarks(uri).collect { bookmarks ->
                val state = _uiState.value
                if (state is ViewerUiState.Success) {
                    _uiState.value = state.copy(
                        bookmarkedPages = bookmarks.map { it.pageIndex }.toSet(),
                    )
                }
            }
        }
    }

    private suspend fun persistReadingProgress(uri: String, page: Int) {
        readingProgressRepository.saveProgress(
            ReadingProgress(
                uri = uri,
                lastPageIndex = page,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun persistRecentDocument(uri: String) {
        val name = currentDisplayName ?: uri.substringAfterLast('/')
        recentDocumentRepository.saveRecentDocument(
            RecentDocument(
                uri = uri,
                displayName = name,
                lastOpenedAt = System.currentTimeMillis(),
            ),
        )
    }

    override fun onCleared() {
        super.onCleared()
        bookmarkObserveJob?.cancel()

        viewModelScope.launch(dispatcherProvider.io) {
            currentSession?.let {
                try {
                    engine?.closeDocument(it)
                } catch (_: Exception) {
                }
            }
        }

        bitmapCache.snapshot().values.forEach { bitmap: Bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        bitmapCache.evictAll()

        thumbnailCache.snapshot().values.forEach { bitmap: Bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        thumbnailCache.evictAll()
    }
}
