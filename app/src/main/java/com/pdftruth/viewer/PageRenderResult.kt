package com.pdftruth.viewer

import android.graphics.Bitmap

data class PageRenderResult(
    val pageIndex: Int,
    val bitmap: Bitmap?,
)