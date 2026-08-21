package com.rendox.shoppinggenius.data.category

import com.rendox.shoppinggenius.data.Syncable
import com.rendox.shoppinggenius.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository : Syncable {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: String): Flow<Category?>
    suspend fun createCategory(name: String): Category
    suspend fun updateCategories(categories: List<Category>)
    suspend fun updateCategoryName(
        categoryId: String,
        name: String
    )
    suspend fun updateCategoryIcon(
        categoryId: String,
        iconId: String?
    )
}
