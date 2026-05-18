package com.pdftruth.viewer.search

import android.content.Context
import android.net.Uri
import com.pdftruth.domain.model.PdfSearchOutcome
import com.pdftruth.domain.model.PdfSearchResult
import com.pdftruth.domain.model.SearchHighlightRange
import io.legere.pdfiumandroid.PdfiumCore
import java.util.LinkedHashMap

class PdfiumSearchEngine(
    private val appContext: Context,
    private val fallback: PdfSearchEngine,
) : PdfSearchEngine {

    private val textCache = object : LinkedHashMap<String, String>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > MAX_TEXT_CACHE
    }

    private val resultCache = object : LinkedHashMap<String, List<PdfSearchResult>>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<PdfSearchResult>>?): Boolean = size > MAX_RESULT_CACHE
    }

    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return PdfSearchOutcome.Empty

        val resultKey = "$documentUri|${normalizedQuery.lowercase()}|$pageCount"
        resultCache[resultKey]?.let { cached ->
            return if (cached.isEmpty()) PdfSearchOutcome.Empty else PdfSearchOutcome.Success(cached)
        }

        val descriptor = appContext.contentResolver.openFileDescriptor(Uri.parse(documentUri), "r")
            ?: return PdfSearchOutcome.Unsupported("문서를 열 수 없어 검색을 시작할 수 없습니다.")

        return try {
            val core = PdfiumCore(appContext)
            val document = core.newDocument(descriptor)
            val availablePageCount = minOf(pageCount, document.getPageCount())
            val results = mutableListOf<PdfSearchResult>()

            for (pageIndex in 0 until availablePageCount) {
                val pageText = getOrExtractPageText(documentUri, pageIndex) {
                    document.openTextPage(pageIndex).use { textPage ->
                        val charCount = textPage.textPageCountChars().coerceAtLeast(0)
                        if (charCount == 0) "" else (textPage.textPageGetText(0, charCount) ?: "")
                    }
                }

                if (pageText.isEmpty()) continue
                val ranges = findRanges(pageText, normalizedQuery)
                if (ranges.isEmpty()) continue

                val snippet = buildSnippet(pageText, ranges.first(), normalizedQuery.length)
                val snippetRanges = findRanges(snippet, normalizedQuery)
                results += PdfSearchResult(
                    pageIndex = pageIndex,
                    summary = snippet,
                    highlightRanges = snippetRanges,
                    matchCount = ranges.size,
                )
            }

            document.close()
            resultCache[resultKey] = results
            if (results.isEmpty()) PdfSearchOutcome.Empty else PdfSearchOutcome.Success(results)
        } catch (e: Throwable) {
            when (val fallbackResult = fallback.search(documentUri, normalizedQuery, pageCount)) {
                is PdfSearchOutcome.Unsupported -> PdfSearchOutcome.Unsupported(
                    "PDFium 검색을 사용할 수 없습니다. ${e.message ?: "초기화 실패"}. ${fallbackResult.message}"
                )
                else -> PdfSearchOutcome.Failure(e.message ?: "PDFium 검색 처리 중 오류")
            }
        }
    }

    private fun getOrExtractPageText(documentUri: String, pageIndex: Int, extractor: () -> String): String {
        val key = "$documentUri#$pageIndex"
        val cached = textCache[key]
        if (cached != null) return cached

        val extracted = extractor()
        textCache[key] = extracted
        return extracted
    }

    private fun findRanges(text: String, query: String): List<SearchHighlightRange> {
        val ranges = mutableListOf<SearchHighlightRange>()
        var index = text.indexOf(query, ignoreCase = true)
        while (index >= 0) {
            ranges += SearchHighlightRange(index, index + query.length)
            index = text.indexOf(query, startIndex = index + query.length, ignoreCase = true)
        }
        return ranges
    }

    private fun buildSnippet(text: String, range: SearchHighlightRange, queryLength: Int): String {
        val start = (range.start - 30).coerceAtLeast(0)
        val end = (range.start + queryLength + 30).coerceAtMost(text.length)
        return text.substring(start, end).replace('\n', ' ').trim()
    }

    companion object {
        private const val MAX_TEXT_CACHE = 120
        private const val MAX_RESULT_CACHE = 24
    }
}
