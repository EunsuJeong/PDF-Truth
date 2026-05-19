package com.pdftruth.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pdftruth.di.AppContainer
import com.pdftruth.ui.screen.MainScreen
import com.pdftruth.ui.screen.ViewerScreen
import com.pdftruth.viewmodel.MainViewModel
import com.pdftruth.viewmodel.ViewerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val container = AppContainer.getInstance(navController.context)

    fun ensureReadPermission(uri: Uri) {
        val resolver = navController.context.contentResolver
        val persisted = resolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }
        if (persisted) return

        try {
            resolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // 최근 문서는 persistable flag가 없을 수 있어 여기서는 예외를 삼키고
                // 실제 open 시점에서 에러 상태로 사용자에게 안내한다.
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Main.route,
        modifier = modifier,
    ) {
        composable(route = AppRoute.Main.route) {
            val viewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(
                            recentDocumentRepository = container.recentDocumentRepository,
                            readingProgressRepository = container.readingProgressRepository,
                        ) as T
                    }
                },
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MainScreen(
                uiState = uiState,
                onPdfPicked = { uri ->
                    // SAF 권한 유지
                    ensureReadPermission(uri)
                    navController.navigate(AppRoute.Viewer.route + "?uri=" + Uri.encode(uri.toString()))
                },
                onRecentDocumentClicked = { uri ->
                    ensureReadPermission(uri)
                    navController.navigate(AppRoute.Viewer.route + "?uri=" + Uri.encode(uri.toString()))
                },
            )
        }

        composable(
            route = AppRoute.Viewer.route + "?uri={uri}",
            arguments = listOf(
                navArgument("uri") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val viewModel: ViewerViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ViewerViewModel(
                            recentDocumentRepository = container.recentDocumentRepository,
                            readingProgressRepository = container.readingProgressRepository,
                            readerPreferencesRepository = container.readerPreferencesRepository,
                            bookmarkRepository = container.bookmarkRepository,
                            pdfSearchRepository = container.pdfSearchRepository,
                            documentRepository = container.documentRepository,
                            context = navController.context,
                        ) as T
                    }
                },
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.let { Uri.parse(it) }

            LaunchedEffect(uriString) {
                if (uri != null) {
                    viewModel.openPdf(uri)
                }
            }

            ViewerScreen(
                uiState = uiState,
                onNavigateUp = { navController.popBackStack() },
                onCurrentPageChanged = viewModel::setCurrentPage,
                onToggleBookmark = viewModel::toggleCurrentPageBookmark,
                onSearchQueryChanged = viewModel::updateSearchQuery,
                onSearchExecute = viewModel::executeSearch,
                onSearchResultClick = viewModel::openSearchResult,
                onToggleThumbnails = viewModel::toggleThumbnails,
                onThumbnailClick = viewModel::openThumbnailPage,
                pdfUri = uri,
            )
        }
    }
}