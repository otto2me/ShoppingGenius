package com.rendox.grocerygenius.feature.listen

sealed interface ListenUiIntent {
    data class OnEditProductName(val productId: String, val newName: String) : ListenUiIntent
    data class OnEditCategoryName(val categoryId: String, val newName: String) : ListenUiIntent
    data class OnEditProductIcon(val productId: String) : ListenUiIntent
    data class OnEditProductCategory(val productId: String, val categoryId: String?) : ListenUiIntent
    data class OnToggleProductFavorite(val productId: String, val isFavorite: Boolean) : ListenUiIntent
    data class OnDeleteProduct(val productId: String) : ListenUiIntent
    data class OnCreateCategory(val name: String) : ListenUiIntent
    data object OnCancelEdit : ListenUiIntent
}