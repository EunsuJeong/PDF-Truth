package com.pdftruth.viewer

data class PageRenderRequest(
    val pageIndex: Int,
    val width: Int,
    val height: Int,
)