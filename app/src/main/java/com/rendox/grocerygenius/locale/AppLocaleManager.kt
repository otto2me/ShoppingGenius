package com.rendox.grocerygenius.locale

import androidx.appcompat.app.AppCompatDelegate
import com.rendox.grocerygenius.datastore.UserPreferencesDataSource
import com.rendox.grocerygenius.model.AppLanguage
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AppLocaleManager @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) {
    suspend fun applySavedLanguage() {
        applyLanguageTag(userPreferencesDataSource.userPreferencesFlow.first().selectedLanguageTag)
    }

    fun applyLanguageTag(languageTag: String?) {
        AppCompatDelegate.setApplicationLocales(AppLanguage.toLocaleListCompat(languageTag))
    }
}

