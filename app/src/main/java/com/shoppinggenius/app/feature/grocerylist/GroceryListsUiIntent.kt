package com.shoppinggenius.app.feature.grocerylist

import androidx.compose.ui.text.input.TextFieldValue
import com.shoppinggenius.app.model.Grocery

sealed interface GroceryListsUiIntent {
    data class OnGroceryItemClick(val item: Grocery) : GroceryListsUiIntent
    data class UpdateGroceryListName(val name: TextFieldValue) : GroceryListsUiIntent
    data object OnKeyboardHidden : GroceryListsUiIntent
    data object OnDeleteGroceryList : GroceryListsUiIntent
    data class OnEditGroceryListToggle(val editModeIsEnabled: Boolean) : GroceryListsUiIntent
    data class OnNavigateToCategoryScreen(val categoryId: String?) : GroceryListsUiIntent
    data object OnShareGroceryList : GroceryListsUiIntent
}
