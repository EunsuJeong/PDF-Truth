package com.pdftruth.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.pdftruth.ui.navigation.AppNavGraph

@Composable
fun PdfTruthApp() {
    val navController = rememberNavController()
    AppNavGraph(navController = navController)
}