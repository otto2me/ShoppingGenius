package com.rendox.shoppinggenius.feature.listen

import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.Product

data class ListenUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val editingProductId: String? = null,
    val editingCategoryId: String? = null,
    val editingProductName: String = "",
    val editingCategoryName: String = ""
)
