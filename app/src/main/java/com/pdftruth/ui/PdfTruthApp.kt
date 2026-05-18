package com.pdftruth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.pdftruth.ui.navigation.AppNavGraph
import com.pdftruth.ui.screen.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun PdfTruthApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(500)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        val navController = rememberNavController()
        AppNavGraph(navController = navController)
    }
}