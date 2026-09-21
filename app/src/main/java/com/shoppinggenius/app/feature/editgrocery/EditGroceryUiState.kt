package com.shoppinggenius.app.feature.editgrocery

import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.Grocery

data class EditGroceryUiState(
    val editGrocery: Grocery? = null,
    val clearEditGroceryNameButtonIsShown: Boolean = false,
    val clearEditGroceryDescriptionButtonIsShown: Boolean = false,
    val groceryCategories: List<Category> = emptyList(),
    val nameCanBeModified: Boolean = false,
    val showRemoveFromListButton: Boolean = true
)
