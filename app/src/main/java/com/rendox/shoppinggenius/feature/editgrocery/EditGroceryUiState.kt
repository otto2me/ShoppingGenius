package com.rendox.shoppinggenius.feature.editgrocery

import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.Grocery

data class EditGroceryUiState(
    val editGrocery: Grocery? = null,
    val clearEditGroceryDescriptionButtonIsShown: Boolean = false,
    val groceryCategories: List<Category> = emptyList(),
    val showRemoveFromListButton: Boolean = true
)
