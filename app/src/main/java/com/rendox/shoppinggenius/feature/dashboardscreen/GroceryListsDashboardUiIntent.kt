package com.rendox.shoppinggenius.feature.dashboardscreen

import com.rendox.shoppinggenius.model.GroceryList

sealed interface GroceryListsDashboardUiIntent {
    data class OnUpdateGroceryLists(val groceryLists: List<GroceryList>) :
        GroceryListsDashboardUiIntent
    data object OnAdderItemClick : GroceryListsDashboardUiIntent
}
