package com.rendox.shoppinggenius.feature.editgrocery

import androidx.compose.ui.text.input.TextFieldValue
import com.rendox.shoppinggenius.model.Category

sealed interface EditGroceryUiIntent {
    data class OnDescriptionChanged(val description: TextFieldValue) : EditGroceryUiIntent
    data object OnClearDescription : EditGroceryUiIntent
    data class OnCategorySelected(val category: Category) : EditGroceryUiIntent
    data object OnCustomCategorySelected : EditGroceryUiIntent
    data class OnCreateCategory(val name: String) : EditGroceryUiIntent
    data object OnToggleFavorite : EditGroceryUiIntent
    data object OnRemoveGroceryFromList : EditGroceryUiIntent
    data object OnDeleteProduct : EditGroceryUiIntent
    data class OnEditProduct(val productId: String) : EditGroceryUiIntent

    data class OnEditOtherGrocery(
        val productId: String,
        val groceryListId: String
    ) : EditGroceryUiIntent
}
