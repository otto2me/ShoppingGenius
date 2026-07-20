package com.rendox.grocerygenius.network.data.sources.category

import com.rendox.grocerygenius.filestorage.LocalAssetDataLoader
import com.rendox.grocerygenius.network.model.CategoryNetwork
import com.rendox.grocerygenius.network.model.NetworkChangeList
import javax.inject.Inject

class OfflineFirstCategoryNetworkDataSource @Inject constructor(
    private val localAssetDataLoader: LocalAssetDataLoader
) : CategoryNetworkDataSource {
    override suspend fun getAllCategories(): List<CategoryNetwork> =
        localAssetDataLoader.loadCategoriesJson()

    override suspend fun getCategoriesByIds(ids: List<String>): List<CategoryNetwork> =
        getAllCategories().filter { it.id in ids }

    override suspend fun getCategoryChangeList(after: Int): List<NetworkChangeList> =
        localAssetDataLoader.loadCategoriesChangeList()
}