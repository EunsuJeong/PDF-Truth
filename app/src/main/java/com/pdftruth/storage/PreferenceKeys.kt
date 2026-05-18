package com.pdftruth.storage

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val lastOpenedDocumentUri = stringPreferencesKey("last_opened_document_uri")
}