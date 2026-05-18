package com.pdftruth.viewer.search

import android.content.Context
import com.pdftruth.domain.model.PdfSearchOutcome
import io.legere.pdfiumandroid.PdfiumCore

class PdfiumSearchEngine(
    private val appContext: Context,
    private val fallback: PdfSearchEngine,
) : PdfSearchEngine {

    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome {
        return try {
            // PDFium 준비 여부만 확인하고, 실제 검색은 안전한 fallback 경로를 사용한다.
            PdfiumCore(appContext)
            fallback.search(documentUri, query, pageCount)
        } catch (e: Throwable) {
            when (val fallbackResult = fallback.search(documentUri, query, pageCount)) {
                is PdfSearchOutcome.Unsupported -> PdfSearchOutcome.Unsupported(
                    "PDFium 검색을 사용할 수 없습니다. ${e.message ?: "초기화 실패"}. ${fallbackResult.message}"
                )
                else -> PdfSearchOutcome.Failure(e.message ?: "PDFium 검색 처리 중 오류")
            }
        }
    }
}
