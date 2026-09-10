package com.shoppinggenius.app.data.category

import com.shoppinggenius.app.data.Syncable
import com.shoppinggenius.app.model.Category
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
