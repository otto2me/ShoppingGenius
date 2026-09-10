package com.shoppinggenius.app.locale

import androidx.appcompat.app.AppCompatDelegate
import com.shoppinggenius.app.datastore.UserPreferencesDataSource
import com.shoppinggenius.app.model.AppLanguage
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
