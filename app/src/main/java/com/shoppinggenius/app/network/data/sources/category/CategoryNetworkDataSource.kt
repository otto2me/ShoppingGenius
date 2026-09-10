package com.shoppinggenius.app.network.data.sources.category

import com.shoppinggenius.app.network.model.CategoryNetwork
import com.shoppinggenius.app.network.model.NetworkChangeList

interface CategoryNetworkDataSource {
    suspend fun getAllCategories(): List<CategoryNetwork>
    suspend fun getCategoriesByIds(ids: List<String>): List<CategoryNetwork>
    suspend fun getCategoryChangeList(after: Int): List<NetworkChangeList>
}
