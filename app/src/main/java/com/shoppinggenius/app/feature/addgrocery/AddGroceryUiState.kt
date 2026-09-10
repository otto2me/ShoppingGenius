package com.shoppinggenius.app.feature.addgrocery

import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.Product

data class AddGroceryUiState(
    val previouslyAddedGrocery: Grocery? = null,
    val bottomSheetContentType: AddGroceryBottomSheetContentType = AddGroceryBottomSheetContentType.Suggestions,
    val grocerySearchResults: List<Grocery> = emptyList(),
    val customProducts: List<Product> = emptyList(),
    val clearSearchQueryButtonIsShown: Boolean = false
)
