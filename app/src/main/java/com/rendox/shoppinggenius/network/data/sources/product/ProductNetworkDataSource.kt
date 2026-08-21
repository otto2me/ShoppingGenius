package com.rendox.shoppinggenius.network.data.sources.product

import com.rendox.shoppinggenius.network.model.NetworkChangeList
import com.rendox.shoppinggenius.network.model.ProductNetwork

interface ProductNetworkDataSource {
    suspend fun getAllProducts(): List<ProductNetwork>
    suspend fun getProductsByIds(ids: List<String>): List<ProductNetwork>
    suspend fun getProductChangeList(after: Int): List<NetworkChangeList>
}
