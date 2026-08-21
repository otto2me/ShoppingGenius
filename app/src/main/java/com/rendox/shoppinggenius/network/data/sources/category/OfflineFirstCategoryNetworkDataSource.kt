package com.rendox.shoppinggenius.network.data.sources.category

import com.rendox.shoppinggenius.data.userpreferences.UserPreferencesRepository
import com.rendox.shoppinggenius.filestorage.LocalAssetDataLoader
import com.rendox.shoppinggenius.network.model.CategoryNetwork
import com.rendox.shoppinggenius.network.model.NetworkChangeList
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class OfflineFirstCategoryNetworkDataSource @Inject constructor(
    private val localAssetDataLoader: LocalAssetDataLoader,
    private val userPreferencesRepository: UserPreferencesRepository
) : CategoryNetworkDataSource {
    override suspend fun getAllCategories(): List<CategoryNetwork> = localAssetDataLoader.loadCategoriesJson(
        userPreferencesRepository.userPreferencesFlow.first().selectedLanguageTag
    )

    override suspend fun getCategoriesByIds(ids: List<String>): List<CategoryNetwork> =
        getAllCategories().filter { it.id in ids }

    override suspend fun getCategoryChangeList(after: Int): List<NetworkChangeList> =
        localAssetDataLoader.loadCategoriesChangeList()
}
