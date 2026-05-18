package com.pdftruth.domain.model

sealed class PdfSearchOutcome {
    data class Success(val results: List<PdfSearchResult>) : PdfSearchOutcome()
    object Empty : PdfSearchOutcome()
    data class Unsupported(val message: String) : PdfSearchOutcome()
    data class Failure(val message: String) : PdfSearchOutcome()
}
