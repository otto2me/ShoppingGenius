package com.rendox.shoppinggenius.feature.iconpicker

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.shoppinggenius.ui.ShoppingGeniusTransition

const val ICON_PICKER_ROUTE = "icon_picker_route"
const val PRODUCT_ID_ARG = "edit_grocery_id_arg"
const val ICON_PICKER_GROCERY_LIST_ID_ARG = "grocery_list_id_arg"
const val ICON_PICKER_ROUTE_WITH_ARGS = "$ICON_PICKER_ROUTE/{$PRODUCT_ID_ARG}/{$ICON_PICKER_GROCERY_LIST_ID_ARG}"

/** Sentinel value: icon picker called from Listen screen (no grocery-list context). */
const val LISTEN_NO_LIST_ID = "__listen_no_list__"

const val CATEGORY_ICON_PICKER_ROUTE = "category_icon_picker_route"
const val CATEGORY_ID_ARG = "category_id_arg"
const val CATEGORY_ICON_PICKER_ROUTE_WITH_ARGS = "$CATEGORY_ICON_PICKER_ROUTE/{$CATEGORY_ID_ARG}"

fun NavController.navigateToIconPicker(
    editProductId: String,
    groceryListId: String,
    navOptions: (NavOptionsBuilder.() -> Unit) = {}
) {
    this.navigate(
        route = "$ICON_PICKER_ROUTE/$editProductId/$groceryListId",
        builder = navOptions
    )
}

fun NavController.navigateToCategoryIconPicker(
    categoryId: String,
    navOptions: (NavOptionsBuilder.() -> Unit) = {}
) {
    this.navigate(
        route = "$CATEGORY_ICON_PICKER_ROUTE/$categoryId",
        builder = navOptions
    )
}

fun NavGraphBuilder.iconPickerScreen(navigateBack: () -> Unit) {
    composable(
        route = ICON_PICKER_ROUTE_WITH_ARGS,
        enterTransition = { ShoppingGeniusTransition.SlideInVertically },
        exitTransition = { ShoppingGeniusTransition.SlideOutVertically },
        arguments = listOf(
            navArgument(PRODUCT_ID_ARG) {
                type = NavType.StringType
            },
            navArgument(ICON_PICKER_GROCERY_LIST_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) {
        IconPickerRoute(
            navigateBack = navigateBack
        )
    }
}

fun NavGraphBuilder.categoryIconPickerScreen(navigateBack: () -> Unit) {
    composable(
        route = CATEGORY_ICON_PICKER_ROUTE_WITH_ARGS,
        enterTransition = { ShoppingGeniusTransition.SlideInVertically },
        exitTransition = { ShoppingGeniusTransition.SlideOutVertically },
        arguments = listOf(
            navArgument(CATEGORY_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) {
        CategoryIconPickerRoute(
            navigateBack = navigateBack
        )
    }
}
