package com.rendox.shoppinggenius.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.shoppinggenius.ui.ShoppingGeniusTransition

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: (NavOptionsBuilder.() -> Unit) = {}) {
    this.navigate(
        route = SETTINGS_ROUTE,
        builder = navOptions
    )
}

fun NavGraphBuilder.settingsScreen(
    navigateBack: () -> Unit,
    navigateToListen: () -> Unit = {}
) {
    composable(
        route = SETTINGS_ROUTE,
        enterTransition = { ShoppingGeniusTransition.SlideInVertically },
        exitTransition = { ShoppingGeniusTransition.SlideOutVertically }
    ) {
        SettingsRoute(
            navigateBack = navigateBack,
            navigateToListen = navigateToListen
        )
    }
}
