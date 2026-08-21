package com.rendox.shoppinggenius.data.icons

import com.rendox.shoppinggenius.data.Syncable
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.IconReference
import kotlinx.coroutines.flow.Flow

interface IconRepository : Syncable {
    fun getIconsGroupedByCategory(): Flow<Map<Category, List<IconReference>>>
    suspend fun getGroceryIconsByName(name: String): List<IconReference>
    suspend fun importCustomIconFromUrl(
        imageUrl: String,
        fallbackImageUrl: String? = null
    ): IconReference?
    suspend fun deleteIcon(uniqueFileName: String)
}
