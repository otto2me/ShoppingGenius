package com.rendox.grocerygenius.model

data class UserPreferences(
    val defaultListId: String? = DEFAULT_USER_PREFERENCES.defaultListId,
    val lastOpenedListId: String? = DEFAULT_USER_PREFERENCES.lastOpenedListId,
    val darkThemeConfig: DarkThemeConfig = DEFAULT_USER_PREFERENCES.darkThemeConfig,
    val useSystemAccentColor: Boolean = DEFAULT_USER_PREFERENCES.useSystemAccentColor,
    val openLastViewedList: Boolean = DEFAULT_USER_PREFERENCES.openLastViewedList,
    val useListViewForGroceries: Boolean = DEFAULT_USER_PREFERENCES.useListViewForGroceries,
    val widgetBackgroundOpacityPercent: Int = DEFAULT_USER_PREFERENCES.widgetBackgroundOpacityPercent,
    val selectedTheme: GroceryGeniusColorScheme = DEFAULT_USER_PREFERENCES.selectedTheme,
    val selectedLanguageTag: String? = DEFAULT_USER_PREFERENCES.selectedLanguageTag,
    val autoDeleteCompletedAfterHours: Int = DEFAULT_USER_PREFERENCES.autoDeleteCompletedAfterHours
)

val DEFAULT_USER_PREFERENCES = UserPreferences(
    defaultListId = null,
    lastOpenedListId = null,
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    useSystemAccentColor = true,
    openLastViewedList = true,
    useListViewForGroceries = false,
    widgetBackgroundOpacityPercent = 30,
    selectedTheme = GroceryGeniusColorScheme.CyanColorScheme,
    selectedLanguageTag = null,
    autoDeleteCompletedAfterHours = 24
)