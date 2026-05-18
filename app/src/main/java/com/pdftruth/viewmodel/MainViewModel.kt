package com.pdftruth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdftruth.domain.repository.ReadingProgressRepository
import com.pdftruth.domain.repository.RecentDocumentRepository
import com.pdftruth.util.DefaultDispatcherProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class MainViewModel(
    private val recentDocumentRepository: RecentDocumentRepository,
    private val readingProgressRepository: ReadingProgressRepository,
) : ViewModel() {

    private val dispatcherProvider = DefaultDispatcherProvider
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            recentDocumentRepository.observeRecentDocuments().collect { recentDocs ->
                val items = recentDocs.map { doc ->
                    val progress = readingProgressRepository.observeProgress(doc.uri).first()
                    RecentDocumentItemUi(
                        uri = doc.uri,
                        displayName = doc.displayName,
                        lastOpenedAt = doc.lastOpenedAt,
                        lastPageIndex = progress?.lastPageIndex,
                    )
                }
                _uiState.value = _uiState.value.copy(recentDocuments = items)
            }
        }
    }
}
