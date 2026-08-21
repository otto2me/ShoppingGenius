package com.rendox.shoppinggenius.feature.settings

import android.net.Uri
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.DarkThemeConfig
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme

sealed interface SettingsScreenIntent {
    data class ChangeDarkThemeConfig(val config: DarkThemeConfig) : SettingsScreenIntent
    data class ChangeUseSystemAccentColor(val use: Boolean) : SettingsScreenIntent
    data class ChangeLanguage(val languageTag: String?) : SettingsScreenIntent
    data class OnChangeDefaultList(val listId: String?) : SettingsScreenIntent
    data class ChangeOpenLastViewedListConfig(val openLastViewedList: Boolean) : SettingsScreenIntent
    data class ChangeUseListViewForGroceries(val useListViewForGroceries: Boolean) : SettingsScreenIntent
    data class ChangeWidgetBackgroundOpacityPercent(val opacityPercent: Int) : SettingsScreenIntent
    data class ChangeColorScheme(val scheme: ShoppingGeniusColorScheme) : SettingsScreenIntent
    data class ChangeAutoDeleteCompletedAfterHours(val hours: Int) : SettingsScreenIntent
    data object OnTestDuckDuckGoImageSearchConnection : SettingsScreenIntent
    data class OnUpdateCategories(val categories: List<Category>) : SettingsScreenIntent
    data object OnResetCategoriesOrder : SettingsScreenIntent
    data class OnExportData(val uri: Uri) : SettingsScreenIntent
    data class OnImportData(val uri: Uri) : SettingsScreenIntent
}


