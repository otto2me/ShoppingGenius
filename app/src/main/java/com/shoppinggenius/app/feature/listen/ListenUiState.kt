package com.shoppinggenius.app.feature.listen

import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.Product

data class ListenUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val editingProductId: String? = null,
    val editingCategoryId: String? = null,
    val editingProductName: String = "",
    val editingCategoryName: String = ""
)
