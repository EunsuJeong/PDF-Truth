package com.pdftruth.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readerPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reader_preferences",
)

class ReaderPreferencesStorage(
    private val context: Context,
) {

    fun observeLastOpenedDocumentUri(): Flow<String?> {
        return context.readerPreferencesDataStore.data.map { preferences ->
            preferences[PreferenceKeys.lastOpenedDocumentUri]
        }
    }

    suspend fun saveLastOpenedDocumentUri(uri: String) {
        context.readerPreferencesDataStore.edit { preferences ->
            preferences[PreferenceKeys.lastOpenedDocumentUri] = uri
        }
    }
}