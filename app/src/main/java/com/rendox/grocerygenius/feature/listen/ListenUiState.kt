package com.rendox.grocerygenius.feature.listen

import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.model.Product

data class ListenUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val editingProductId: String? = null,
    val editingCategoryId: String? = null,
    val editingProductName: String = "",
    val editingCategoryName: String = ""
)

