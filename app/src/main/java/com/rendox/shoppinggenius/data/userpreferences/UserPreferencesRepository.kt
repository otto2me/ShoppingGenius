package com.rendox.shoppinggenius.data.userpreferences

import com.rendox.shoppinggenius.model.DarkThemeConfig
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme
import com.rendox.shoppinggenius.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateDefaultListId(listId: String?)
    suspend fun updateLastOpenedListId(listId: String)
    suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun updateUseSystemAccentColor(useSystemAccentColor: Boolean)
    suspend fun updateOpenLastViewedList(openLastViewedList: Boolean)
    suspend fun updateUseListViewForGroceries(useListViewForGroceries: Boolean)
    suspend fun updateGroupByCategoryInListMode(groupByCategoryInListMode: Boolean)
    suspend fun updateWidgetBackgroundOpacityPercent(opacityPercent: Int)
    suspend fun updateSelectedTheme(selectedTheme: ShoppingGeniusColorScheme)
    suspend fun updateSelectedLanguageTag(selectedLanguageTag: String?)
    suspend fun updateAutoDeleteCompletedAfterHours(hours: Int)
    suspend fun getGroceryListIdToOpenOnStartup(): String?
}
