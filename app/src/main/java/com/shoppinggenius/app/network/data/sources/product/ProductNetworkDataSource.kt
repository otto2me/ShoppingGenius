package com.shoppinggenius.app.network.data.sources.product

import com.shoppinggenius.app.network.model.NetworkChangeList
import com.shoppinggenius.app.network.model.ProductNetwork

interface ProductNetworkDataSource {
    suspend fun getAllProducts(): List<ProductNetwork>
    suspend fun getProductsByIds(ids: List<String>): List<ProductNetwork>
    suspend fun getProductChangeList(after: Int): List<NetworkChangeList>
}
