package com.pdftruth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.pdftruth.viewmodel.MainUiState

@Composable
fun MainScreen(
    uiState: MainUiState,
    onPdfPicked: (Uri) -> Unit,
    onRecentDocumentClicked: (Uri) -> Unit,
) {
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onPdfPicked(uri)
            }
        }
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = uiState.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            )
            Button(
                onClick = {
                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                },
                enabled = uiState.canEnterViewer,
            ) {
                Text(text = "PDF 파일 선택")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "최근 문서",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.recentDocuments.isEmpty()) {
                    item {
                        Text(
                            text = "최근 문서가 없습니다. 먼저 PDF 파일을 선택해 주세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        )
                    }
                }
                items(uiState.recentDocuments, key = { it.uri }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.displayName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "마지막 페이지: ${item.lastPageIndex?.plus(1) ?: 1}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Button(onClick = { onRecentDocumentClicked(Uri.parse(item.uri)) }) {
                            Text("열기")
                        }
                    }
                }
            }
        }
    }
}