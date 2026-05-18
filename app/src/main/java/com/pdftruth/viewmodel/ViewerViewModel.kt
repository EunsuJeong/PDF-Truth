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
import com.pdftruth.viewer.PdfDocumentSource
import com.pdftruth.viewer.PageRenderRequest
import com.pdftruth.viewer.engine.AndroidPdfRendererEngine
import com.pdftruth.util.DefaultDispatcherProvider


class ViewerViewModel : ViewModel() {
    private val dispatcherProvider = DefaultDispatcherProvider
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Idle)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private var currentSession: com.pdftruth.viewer.PdfDocumentSession? = null
    private var engine: AndroidPdfRendererEngine? = null
    private var pageStates: MutableList<PageUiState> = mutableListOf()
    private var _currentPage: Int = 0
    // 간단한 페이지별 Bitmap 캐시 (LRU 등 확장 가능)
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    fun openPdf(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ViewerUiState.Error("PDF 파일이 선택되지 않았습니다.")
            return
        }
        val context = getApplicationContextSafely()
        engine = AndroidPdfRendererEngine(context)
        _uiState.value = ViewerUiState.Loading
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val source = PdfDocumentSource(uri.toString())
                val session = engine!!.openDocument(source)
                currentSession = session
                val pageCount = session.pageCount
                pageStates = MutableList(pageCount) { PageUiState.Loading }
                _currentPage = 0
                // 최초 상태 전달
                _uiState.value = ViewerUiState.Success(
                    pageCount = pageCount,
                    pages = pageStates.toList(),
                    fileName = null,
                    currentPage = _currentPage
                )
                // 각 페이지 비동기 렌더링
                for (i in 0 until pageCount) {
                    launch(dispatcherProvider.io) {
                        try {
                            // 캐시 우선
                            val cached = bitmapCache[i]
                            val bitmap = if (cached != null) cached else {
                                val pageBitmapResult = engine!!.renderPage(
                                    session,
                                    PageRenderRequest(pageIndex = i, width = 1080, height = 1440)
                                )
                                pageBitmapResult.bitmap?.also { bitmapCache[i] = it }
                            }
                            if (bitmap != null) {
                                pageStates[i] = PageUiState.BitmapReady(bitmap)
                            } else {
                                pageStates[i] = PageUiState.Error("페이지 렌더링 실패")
                            }
                        } catch (e: Exception) {
                            pageStates[i] = PageUiState.Error("페이지 렌더링 실패: ${e.localizedMessage}")
                        }
                        // 상태 갱신
                        _uiState.value = ViewerUiState.Success(
                            pageCount = pageCount,
                            pages = pageStates.toList(),
                            fileName = null,
                            currentPage = _currentPage
                        )
                    }
                }
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