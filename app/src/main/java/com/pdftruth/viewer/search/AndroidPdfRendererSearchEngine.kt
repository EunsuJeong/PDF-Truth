package com.pdftruth.viewer.search

import com.pdftruth.domain.model.PdfSearchOutcome

class AndroidPdfRendererSearchEngine : PdfSearchEngine {
    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome {
        return PdfSearchOutcome.Unsupported("현재 엔진은 본문 텍스트 검색을 지원하지 않습니다. PDFium 기반 엔진 도입 필요.")
    }
}
