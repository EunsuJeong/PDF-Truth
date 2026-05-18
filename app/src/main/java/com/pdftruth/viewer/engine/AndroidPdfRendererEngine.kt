package com.pdftruth.viewer.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.pdftruth.viewer.PageRenderRequest
import com.pdftruth.viewer.PageRenderResult
import com.pdftruth.viewer.PdfDocumentSession
import com.pdftruth.viewer.PdfDocumentSource
import com.pdftruth.viewer.PdfEngine
import java.io.IOException

class AndroidPdfRendererEngine(
    private val context: Context,
) : PdfEngine {

    private data class Session(
        val pfd: ParcelFileDescriptor,
        val renderer: PdfRenderer,
    ) : PdfDocumentSession {
        override val pageCount: Int get() = renderer.pageCount
    }

    override suspend fun openDocument(source: PdfDocumentSource): PdfDocumentSession {
        val uri = android.net.Uri.parse(source.uri)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IOException("파일 디스크립터를 열 수 없습니다.")
        val renderer = PdfRenderer(pfd)
        return Session(pfd, renderer)
    }

    override suspend fun renderPage(
        session: PdfDocumentSession,
        request: PageRenderRequest,
    ): PageRenderResult {
        val s = session as? Session ?: throw IllegalArgumentException("세션 타입 불일치")
        val pageIndex = request.pageIndex.coerceIn(0, s.renderer.pageCount - 1)
        val page = s.renderer.openPage(pageIndex)
        val bitmap = Bitmap.createBitmap(request.width, request.height, Bitmap.Config.ARGB_8888)
        try {
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        } finally {
            page.close()
        }
        return PageRenderResult(pageIndex, bitmap)
    }

    override suspend fun closeDocument(session: PdfDocumentSession) {
        val s = session as? Session ?: return
        try {
            s.renderer.close()
        } catch (_: Exception) {}
        try {
            s.pfd.close()
        } catch (_: Exception) {}
    }
}