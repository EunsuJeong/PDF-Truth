package com.pdftruth.viewer.search

import android.content.Context
import android.net.Uri
import com.pdftruth.domain.model.PdfSearchOutcome
import com.pdftruth.domain.model.PdfSearchResult
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
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return PdfSearchOutcome.Empty

        val fileDescriptor = appContext.contentResolver.openFileDescriptor(Uri.parse(documentUri), "r")
            ?: return PdfSearchOutcome.Unsupported("문서를 열 수 없어 검색을 시작할 수 없습니다.")

        return try {
            val core = PdfiumCore(appContext)
            val document = core.newDocument(fileDescriptor)
            val lastPage = (minOf(pageCount, document.getPageCount()) - 1).coerceAtLeast(-1)
            val results = mutableListOf<PdfSearchResult>()

            for (pageIndex in 0..lastPage) {
                val pageText = document.openTextPage(pageIndex).use { textPage ->
                    val charCount = textPage.textPageCountChars().coerceAtLeast(0)
                    if (charCount == 0) "" else (textPage.textPageGetText(0, charCount) ?: "")
                }

                if (pageText.contains(normalizedQuery, ignoreCase = true)) {
                    results += PdfSearchResult(
                        pageIndex = pageIndex,
                        summary = buildSnippet(pageText, normalizedQuery),
                    )
                }
            }

            document.close()

            if (results.isEmpty()) PdfSearchOutcome.Empty else PdfSearchOutcome.Success(results)
        } catch (e: Throwable) {
            val fallbackResult = fallback.search(documentUri, normalizedQuery, pageCount)
            if (fallbackResult is PdfSearchOutcome.Unsupported) {
                PdfSearchOutcome.Unsupported(
                    "PDFium 검색을 사용할 수 없습니다. ${e.message ?: "초기화 실패"}. 현재 엔진은 본문 텍스트 검색을 지원하지 않습니다."
                )
            } else {
                PdfSearchOutcome.Failure(e.message ?: "PDFium 검색 처리 중 오류")
            }
        }
    }

    private fun buildSnippet(text: String, query: String): String {
        val index = text.indexOf(query, ignoreCase = true)
        if (index < 0) return text.take(80)

        val start = (index - 30).coerceAtLeast(0)
        val end = (index + query.length + 30).coerceAtMost(text.length)
        return text.substring(start, end).replace('\n', ' ').trim()
    }
}
