package com.rendox.shoppinggenius.network.data.sources.category

import com.rendox.shoppinggenius.network.model.CategoryNetwork
import com.rendox.shoppinggenius.network.model.NetworkChangeList

interface CategoryNetworkDataSource {
    suspend fun getAllCategories(): List<CategoryNetwork>
    suspend fun getCategoriesByIds(ids: List<String>): List<CategoryNetwork>
    suspend fun getCategoryChangeList(after: Int): List<NetworkChangeList>
}
