package com.pdftruth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.pdftruth.viewer.PdfDocumentSource
import com.pdftruth.viewer.PageRenderRequest
import com.pdftruth.viewer.engine.AndroidPdfRendererEngine
import com.pdftruth.util.DefaultDispatcherProvider

// 실제 프로젝트에서는 DI 또는 Application context 주입 필요
// 아래 예시는 context를 안전하게 주입하는 구조로 교체 필요
class ViewerViewModel : ViewModel() {
    private val dispatcherProvider = DefaultDispatcherProvider
    // TODO: context 주입 구조로 교체 필요
    private val pdfEngine: AndroidPdfRendererEngine? = null
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Idle)
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private var currentSession: com.pdftruth.viewer.PdfDocumentSession? = null

    fun openPdf(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ViewerUiState.Error("PDF 파일이 선택되지 않았습니다.")
            return
        }
        // 실제 환경에서는 context 주입 필요
        val context = getApplicationContextSafely()
        val engine = AndroidPdfRendererEngine(context)
        _uiState.value = ViewerUiState.Loading
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val source = PdfDocumentSource(uri.toString())
                val session = engine.openDocument(source)
                currentSession = session
                val pageCount = session.pageCount
                val pageBitmapResult = engine.renderPage(
                    session,
                    PageRenderRequest(pageIndex = 0, width = 1080, height = 1440)
                )
                val bitmap = pageBitmapResult.bitmap
                if (bitmap != null) {
                    _uiState.value = ViewerUiState.Success(
                        bitmap = bitmap,
                        pageCount = pageCount,
                        fileName = null // 추후 파일명 추출
                    )
                } else {
                    _uiState.value = ViewerUiState.Error("PDF 페이지 렌더링 실패")
                }
            } catch (e: Exception) {
                _uiState.value = ViewerUiState.Error("PDF 열기 실패: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(dispatcherProvider.io) {
            currentSession?.let {
                try {
                    pdfEngine.closeDocument(it)
                } catch (_: Exception) {}
            }
        }
    }
}

private fun getApplicationContextSafely(): Context {
    // 실제 프로젝트에서는 DI 또는 Application context 주입 필요
    // 임시: ApplicationProvider.getApplicationContext() 등으로 대체 가능
    throw NotImplementedError("Application context를 안전하게 주입해야 합니다.")
}