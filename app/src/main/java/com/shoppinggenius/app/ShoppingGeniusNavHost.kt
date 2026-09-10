package com.shoppinggenius.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.shoppinggenius.app.feature.dashboardscreen.GROCERY_LISTS_DASHBOARD_ROUTE
import com.shoppinggenius.app.feature.dashboardscreen.groceryListsDashboardScreen
import com.shoppinggenius.app.feature.dashboardscreen.navigateToGroceryListsDashboard
import com.shoppinggenius.app.feature.grocerylist.GROCERY_LIST_CATEGORY_NESTED_NAV_ROUTE_WITH_ARGS
import com.shoppinggenius.app.feature.grocerylist.GROCERY_LIST_ROUTE
import com.shoppinggenius.app.feature.grocerylist.groceryListNestedNavigation
import com.shoppinggenius.app.feature.grocerylist.navigateToGroceryList
import com.shoppinggenius.app.feature.iconpicker.LISTEN_NO_LIST_ID
import com.shoppinggenius.app.feature.iconpicker.categoryIconPickerScreen
import com.shoppinggenius.app.feature.iconpicker.iconPickerScreen
import com.shoppinggenius.app.feature.iconpicker.navigateToCategoryIconPicker
import com.shoppinggenius.app.feature.iconpicker.navigateToIconPicker
import com.shoppinggenius.app.feature.listen.listenScreen
import com.shoppinggenius.app.feature.listen.navigateToListen
import com.shoppinggenius.app.feature.settings.navigateToSettings
import com.shoppinggenius.app.feature.settings.settingsScreen

@Composable
fun ShoppingGeniusNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = GROCERY_LISTS_DASHBOARD_ROUTE,
    defaultGroceryListId: String? = null
) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navController,
        startDestination = startDestination
    ) {
        groceryListsDashboardScreen(
            navigateToGroceryListScreen = { groceryListId ->
                if (navController.currentDestination?.route == GROCERY_LISTS_DASHBOARD_ROUTE) {
                    navController.navigateToGroceryList(groceryListId)
                }
            },
            navigateToSettingsScreen = {
                if (navController.currentDestination?.route == GROCERY_LISTS_DASHBOARD_ROUTE) {
                    navController.navigateToSettings()
                }
            }
        )
        groceryListNestedNavigation(
            navController = navController,
            defaultGroceryListId = defaultGroceryListId,
            navigateBack = {
                if (navController.currentDestination?.route == GROCERY_LIST_ROUTE) {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigateToGroceryListsDashboard {
                            popUpTo(route = GROCERY_LIST_CATEGORY_NESTED_NAV_ROUTE_WITH_ARGS) {
                                inclusive = true
                            }
                        }
                    }
                }
            },
            navigateToIconPicker = { editProductId, groceryListId ->
                navController.navigateToIconPicker(
                    editProductId = editProductId,
                    groceryListId = groceryListId
                )
            }
        )
        settingsScreen(
            navigateBack = { navController.popBackStack() },
            navigateToListen = {
                navController.navigateToListen()
            }
        )
        iconPickerScreen(
            navigateBack = { navController.popBackStack() }
        )
        listenScreen(
            navigateBack = { navController.popBackStack() },
            navigateToProductIconPicker = { productId ->
                navController.navigateToIconPicker(
                    editProductId = productId,
                    groceryListId = LISTEN_NO_LIST_ID
                )
            },
            navigateToIconPickerForCategory = { categoryId ->
                navController.navigateToCategoryIconPicker(categoryId)
            }
        )
        categoryIconPickerScreen(
            navigateBack = { navController.popBackStack() }
        )
    }
}
