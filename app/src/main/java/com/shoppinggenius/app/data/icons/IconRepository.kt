package com.shoppinggenius.app.data.icons

import com.shoppinggenius.app.data.Syncable
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.IconReference
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
