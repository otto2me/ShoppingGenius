package com.shoppinggenius.app

import com.shoppinggenius.app.feature.dashboardscreen.GROCERY_LISTS_DASHBOARD_ROUTE
import com.shoppinggenius.app.model.DEFAULT_USER_PREFERENCES
import com.shoppinggenius.app.model.DarkThemeConfig
import com.shoppinggenius.app.model.ShoppingGeniusColorScheme

data class MainActivityUiState(
    val defaultListId: String? = null,
    val startDestinationRoute: String = GROCERY_LISTS_DASHBOARD_ROUTE,
    val darkThemeConfig: DarkThemeConfig = DEFAULT_USER_PREFERENCES.darkThemeConfig,
    val useSystemAccentColor: Boolean = DEFAULT_USER_PREFERENCES.useSystemAccentColor,
    val selectedTheme: ShoppingGeniusColorScheme = DEFAULT_USER_PREFERENCES.selectedTheme
)
