package com.pdftruth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pdftruth.ui.PdfTruthApp
import com.pdftruth.ui.theme.PdfTruthTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfTruthTheme {
                PdfTruthApp()
            }
        }
    }
}