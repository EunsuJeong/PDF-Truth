package com.pdftruth.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.pdftruth.domain.model.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPdfFileDataSource(
    private val context: Context,
) : PdfFileDataSource {

    override suspend fun getDocument(uri: String): PdfDocument = withContext(Dispatchers.IO) {
        val parsedUri = Uri.parse(uri)
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)

        context.contentResolver.query(parsedUri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (cursor.moveToFirst()) {
                val displayName = if (nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else {
                    parsedUri.lastPathSegment.orEmpty()
                }
                val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    cursor.getLong(sizeIndex)
                } else {
                    null
                }

                return@withContext PdfDocument(
                    uri = uri,
                    displayName = displayName,
                    fileSizeBytes = size,
                )
            }
        }

        PdfDocument(
            uri = uri,
            displayName = parsedUri.lastPathSegment ?: uri,
            fileSizeBytes = null,
        )
    }
}