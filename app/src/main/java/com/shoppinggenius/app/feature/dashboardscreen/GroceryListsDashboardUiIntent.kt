package com.shoppinggenius.app.feature.dashboardscreen

import com.shoppinggenius.app.model.GroceryList

sealed interface GroceryListsDashboardUiIntent {
    data class OnUpdateGroceryLists(val groceryLists: List<GroceryList>) :
        GroceryListsDashboardUiIntent
    data object OnAdderItemClick : GroceryListsDashboardUiIntent
}
