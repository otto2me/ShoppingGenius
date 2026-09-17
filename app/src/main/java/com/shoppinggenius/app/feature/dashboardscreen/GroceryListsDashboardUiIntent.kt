package com.shoppinggenius.app.feature.dashboardscreen

import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.GroceryListType

sealed interface GroceryListsDashboardUiIntent {
    data class OnUpdateGroceryLists(val groceryLists: List<GroceryList>) :
        GroceryListsDashboardUiIntent
    data object OnAdderItemClick : GroceryListsDashboardUiIntent
    data class OnCreateGroceryList(val type: GroceryListType) : GroceryListsDashboardUiIntent
}
