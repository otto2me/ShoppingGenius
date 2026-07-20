package com.rendox.grocerygenius.network.data.sources.product

import com.rendox.grocerygenius.filestorage.LocalAssetDataLoader
import com.rendox.grocerygenius.network.model.NetworkChangeList
import com.rendox.grocerygenius.network.model.ProductNetwork
import javax.inject.Inject

class OfflineFirstProductNetworkDataSource @Inject constructor(
    private val localAssetDataLoader: LocalAssetDataLoader
) : ProductNetworkDataSource {
    override suspend fun getAllProducts(): List<ProductNetwork> =
        localAssetDataLoader.loadProductsJson()

    override suspend fun getProductsByIds(ids: List<String>): List<ProductNetwork> =
        getAllProducts().filter { it.id in ids }

    override suspend fun getProductChangeList(after: Int): List<NetworkChangeList> =
        localAssetDataLoader.loadProductsChangeList().filter { it.changeListVersion > after }
}