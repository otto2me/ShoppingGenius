package com.shoppinggenius.app.feature.addgrocery

import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.Product

sealed interface AddGroceryUiIntent {
    data class OnUpdateSearchQuery(val query: String) : AddGroceryUiIntent
    data class OnGrocerySearchResultClick(val grocery: Grocery) : AddGroceryUiIntent
    data object OnSearchFieldKeyboardDone : AddGroceryUiIntent
    data object OnClearSearchQuery : AddGroceryUiIntent
    data object OnAddGroceryBottomSheetCollapsing : AddGroceryUiIntent
    data class OnAddGroceryBottomSheetExpanded(
        val groceryListId: String,
        val isTodoList: Boolean
    ) : AddGroceryUiIntent
    data class OnCustomProductClick(val customProduct: Product) : AddGroceryUiIntent
}
