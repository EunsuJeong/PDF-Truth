package com.pdftruth.domain.model

data class SearchHighlightRange(
    val start: Int,
    val endExclusive: Int,
)

data class PdfSearchResult(
    val pageIndex: Int,
    val summary: String,
    val highlightRanges: List<SearchHighlightRange> = emptyList(),
    val matchCount: Int = 0,
)