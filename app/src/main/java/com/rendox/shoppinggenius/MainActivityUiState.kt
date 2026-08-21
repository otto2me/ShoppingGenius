package com.rendox.shoppinggenius

import com.rendox.shoppinggenius.feature.dashboardscreen.GROCERY_LISTS_DASHBOARD_ROUTE
import com.rendox.shoppinggenius.model.DEFAULT_USER_PREFERENCES
import com.rendox.shoppinggenius.model.DarkThemeConfig
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme

data class MainActivityUiState(
    val defaultListId: String? = null,
    val startDestinationRoute: String = GROCERY_LISTS_DASHBOARD_ROUTE,
    val darkThemeConfig: DarkThemeConfig = DEFAULT_USER_PREFERENCES.darkThemeConfig,
    val useSystemAccentColor: Boolean = DEFAULT_USER_PREFERENCES.useSystemAccentColor,
    val selectedTheme: ShoppingGeniusColorScheme = DEFAULT_USER_PREFERENCES.selectedTheme
)
