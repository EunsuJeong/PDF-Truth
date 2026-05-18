package com.pdftruth.ui.navigation

sealed class AppRoute(val route: String) {
    data object Main : AppRoute("main")
    data object Viewer : AppRoute("viewer")
}