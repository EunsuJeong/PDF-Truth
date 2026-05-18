package com.pdftruth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PdfTruthColorScheme = lightColorScheme()

@Composable
fun PdfTruthTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PdfTruthColorScheme,
        typography = Typography(),
        content = content,
    )
}