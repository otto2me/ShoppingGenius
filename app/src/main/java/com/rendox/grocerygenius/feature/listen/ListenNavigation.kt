package com.rendox.grocerygenius.feature.listen

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.grocerygenius.ui.GroceryGeniusTransition

const val LISTEN_ROUTE = "listen_route"

fun NavController.navigateToListen(navOptions: (NavOptionsBuilder.() -> Unit) = {}) {
    this.navigate(
        route = LISTEN_ROUTE,
        builder = navOptions
    )
}

fun NavGraphBuilder.listenScreen(
    navigateBack: () -> Unit,
    navigateToProductIconPicker: (productId: String) -> Unit,
    navigateToIconPickerForCategory: (categoryId: String) -> Unit
) {
    composable(
        route = LISTEN_ROUTE,
        enterTransition = { GroceryGeniusTransition.SlideInVertically },
        exitTransition = { GroceryGeniusTransition.SlideOutVertically }
    ) {
        ListenRoute(
            navigateBack = navigateBack,
            navigateToProductIconPicker = { productId ->
                navigateToProductIconPicker(productId)
            },
            navigateToIconPickerForCategory = navigateToIconPickerForCategory
        )
    }
}