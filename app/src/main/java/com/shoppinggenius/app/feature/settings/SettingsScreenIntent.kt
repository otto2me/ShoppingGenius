package com.shoppinggenius.app.feature.settings

import android.net.Uri
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.DarkThemeConfig
import com.shoppinggenius.app.model.ShoppingGeniusColorScheme

sealed interface SettingsScreenIntent {
    data class ChangeDarkThemeConfig(val config: DarkThemeConfig) : SettingsScreenIntent
    data class ChangeUseSystemAccentColor(val use: Boolean) : SettingsScreenIntent
    data class ChangeLanguage(val languageTag: String?) : SettingsScreenIntent
    data class OnChangeDefaultList(val listId: String?) : SettingsScreenIntent
    data class ChangeOpenLastViewedListConfig(val openLastViewedList: Boolean) : SettingsScreenIntent
    data class ChangeUseListViewForGroceries(val useListViewForGroceries: Boolean) : SettingsScreenIntent
    data class ChangeGroupByCategoryInListMode(val groupByCategoryInListMode: Boolean) : SettingsScreenIntent
    @Suppress("unused")
    data class ChangeUseThreeLineTodoEntries(val useThreeLineTodoEntries: Boolean) : SettingsScreenIntent
    data class ChangeWidgetBackgroundOpacityPercent(val opacityPercent: Int) : SettingsScreenIntent
    data class ChangeColorScheme(val scheme: ShoppingGeniusColorScheme) : SettingsScreenIntent
    data class ChangeAutoDeleteCompletedAfterHours(val hours: Int) : SettingsScreenIntent
    data object OnTestDuckDuckGoImageSearchConnection : SettingsScreenIntent
    data class OnUpdateCategories(val categories: List<Category>) : SettingsScreenIntent
    data object OnResetCategoriesOrder : SettingsScreenIntent
    data class OnExportData(val uri: Uri) : SettingsScreenIntent
    data class OnImportData(val uri: Uri) : SettingsScreenIntent
}


