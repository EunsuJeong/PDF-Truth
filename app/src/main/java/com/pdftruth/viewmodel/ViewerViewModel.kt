package com.pdftruth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.content.Context
import android.graphics.Bitmap
import com.pdftruth.util.PageBitmapLruCache
import com.pdftruth.domain.model.RecentDocument
import com.pdftruth.domain.repository.RecentDocumentRepository
import com.pdftruth.storage.ReaderPreferencesStorage
import com.pdftruth.viewer.PdfDocumentSource
import com.pdftruth.viewer.PageRenderRequest
import com.pdftruth.viewer.engine.AndroidPdfRendererEngine
import com.pdftruth.util.DefaultDispatcherProvider



class ViewerViewModel(
    private val recentDocumentRepository: RecentDocumentRepository,
    private val readerPreferencesStorage: ReaderPreferencesStorage,
    private val context: Context
) : ViewModel() {
    private val dispatcherProvider = DefaultDispatcherProvider
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Idle)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private var currentSession: com.pdftruth.viewer.PdfDocumentSession? = null
    private var engine: AndroidPdfRendererEngine? = null
    private var pageStates: MutableList<PageUiState> = mutableListOf()
    private var _currentPage: Int = 0
    // LRU 기반 Bitmap 캐시 (현재 페이지 ±2)
    private val bitmapCache = PageBitmapLruCache(5)
    private var currentUri: String? = null

    fun openPdf(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ViewerUiState.Error("PDF 파일이 선택되지 않았습니다.")
            return
        }
        currentUri = uri.toString()
        engine = AndroidPdfRendererEngine(context)
        _uiState.value = ViewerUiState.Loading
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val source = PdfDocumentSource(uri.toString())
                val session = engine!!.openDocument(source)
                currentSession = session
                val pageCount = session.pageCount
                pageStates = MutableList(pageCount) { PageUiState.Loading }
                // 마지막 읽은 페이지 복원
                var restoredPage = 0
                readerPreferencesStorage.observeLastOpenedDocumentUri().collect { lastUri ->
                    if (lastUri == uri.toString()) {
                        // Room에서 마지막 페이지 조회 (구현 필요)
                        // restoredPage = ...
                    }
                }
                _currentPage = restoredPage
                _uiState.value = ViewerUiState.Success(
                    pageCount = pageCount,
                    pages = pageStates.toList(),
                    fileName = null,
                    currentPage = _currentPage
                )
                // 각 페이지 비동기 렌더링 (LRU)
                for (i in 0 until pageCount) {
                    launch(dispatcherProvider.io) {
                        try {
                            val cached = bitmapCache[i]
                            val bitmap = if (cached != null) cached else {
                                val pageBitmapResult = engine!!.renderPage(
                                    session,
                                    PageRenderRequest(pageIndex = i, width = 1080, height = 1440)
                                )
                                pageBitmapResult.bitmap?.also { bitmapCache.put(i, it) }
                            }
                            if (bitmap != null) {
                                pageStates[i] = PageUiState.BitmapReady(bitmap)
                            } else {
                                pageStates[i] = PageUiState.Error("페이지 렌더링 실패")
                            }
                        } catch (e: Exception) {
                            pageStates[i] = PageUiState.Error("페이지 렌더링 실패: ${e.localizedMessage}")
                        }
                        _uiState.value = ViewerUiState.Success(
                            pageCount = pageCount,
                            pages = pageStates.toList(),
                            fileName = null,
                            currentPage = _currentPage
                        )
                    }
                }
                // 최근 문서 자동 저장
                recentDocumentRepository.saveRecentDocument(
                    RecentDocument(
                        uri = uri.toString(),
                        displayName = uri.lastPathSegment ?: "PDF",
                        lastOpenedAt = System.currentTimeMillis()
                    )
                )
                // 마지막 열람 문서 DataStore 저장
                readerPreferencesStorage.saveLastOpenedDocumentUri(uri.toString())
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error("PDF 열기 실패: ${e.localizedMessage}")
            }
        }
    }

    fun setCurrentPage(page: Int) {
        _currentPage = page
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            _uiState.value = state.copy(currentPage = page)
        }
        // 마지막 읽은 페이지 Room 저장
        viewModelScope.launch(dispatcherProvider.io) {
            currentUri?.let { uri ->
                recentDocumentRepository.saveRecentDocument(
                    RecentDocument(
                        uri = uri,
                        displayName = uri.substringAfterLast('/'),
                        lastOpenedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun nextPage() {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            val next = (state.currentPage + 1).coerceAtMost(state.pageCount - 1)
            setCurrentPage(next)
        }
    }

    fun prevPage() {
        val state = _uiState.value
        if (state is ViewerUiState.Success) {
            val prev = (state.currentPage - 1).coerceAtLeast(0)
            setCurrentPage(prev)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(dispatcherProvider.io) {
            currentSession?.let {
                try {
                    engine?.closeDocument(it)
                } catch (_: Exception) {}
            }
        }
    }
}

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(dispatcherProvider.io) {
            currentSession?.let {
                try {
                    engine?.closeDocument(it)
                } catch (_: Exception) {}
            }
        }
    }
}

private fun getApplicationContextSafely(): Context {
    throw NotImplementedError("Application context를 안전하게 주입해야 합니다.")
}