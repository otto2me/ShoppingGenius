package com.shoppinggenius.app.network.data.sources.product

import com.shoppinggenius.app.data.userpreferences.UserPreferencesRepository
import com.shoppinggenius.app.filestorage.LocalAssetDataLoader
import com.shoppinggenius.app.network.model.NetworkChangeList
import com.shoppinggenius.app.network.model.ProductNetwork
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class OfflineFirstProductNetworkDataSource @Inject constructor(
    private val localAssetDataLoader: LocalAssetDataLoader,
    private val userPreferencesRepository: UserPreferencesRepository
) : ProductNetworkDataSource {
    override suspend fun getAllProducts(): List<ProductNetwork> = localAssetDataLoader.loadProductsJson(
        userPreferencesRepository.userPreferencesFlow.first().selectedLanguageTag
    )

    override suspend fun getProductsByIds(ids: List<String>): List<ProductNetwork> =
        getAllProducts().filter { it.id in ids }

    override suspend fun getProductChangeList(after: Int): List<NetworkChangeList> =
        localAssetDataLoader.loadProductsChangeList().filter { it.changeListVersion > after }
}
