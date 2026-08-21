package com.rendox.shoppinggenius.feature.addgrocery

import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.Product

data class AddGroceryUiState(
    val previouslyAddedGrocery: Grocery? = null,
    val bottomSheetContentType: AddGroceryBottomSheetContentType = AddGroceryBottomSheetContentType.Suggestions,
    val grocerySearchResults: List<Grocery> = emptyList(),
    val customProducts: List<Product> = emptyList(),
    val clearSearchQueryButtonIsShown: Boolean = false
)
