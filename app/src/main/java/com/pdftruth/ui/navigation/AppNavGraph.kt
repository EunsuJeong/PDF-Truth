package com.pdftruth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pdftruth.ui.screen.MainScreen
import com.pdftruth.ui.screen.ViewerScreen
import com.pdftruth.viewmodel.MainViewModel
import com.pdftruth.viewmodel.ViewerViewModel
import android.net.Uri
import androidx.compose.runtime.remember

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Main.route,
        modifier = modifier,
    ) {
        composable(route = AppRoute.Main.route) {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MainScreen(
                uiState = uiState,
                onPdfPicked = { uri ->
                    // SAF 권한 유지
                    navController.context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    navController.navigate(AppRoute.Viewer.route + "?uri=" + Uri.encode(uri.toString()))
                },
            )
        }

        composable(
            route = AppRoute.Viewer.route + "?uri={uri}",
            arguments = listOf(
                navArgument("uri") { nullable = true }
            )
        ) { backStackEntry ->
            val viewModel: ViewerViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val uriString = backStackEntry.arguments?.getString("uri")
            val uri = uriString?.let { Uri.parse(it) }

            // PDF 파일이 선택되어 있으면 첫 진입 시 openPdf 호출
            // 실제 환경에서는 rememberSaveable 등으로 중복 호출 방지 필요
            if (uri != null) {
                viewModel.openPdf(uri)
            }

            ViewerScreen(
                uiState = uiState,
                onNavigateUp = { navController.popBackStack() },
                pdfUri = uri,
            )
        }
    }
}