package com.shoppinggenius.app.network.data.sources.category

import com.shoppinggenius.app.data.userpreferences.UserPreferencesRepository
import com.shoppinggenius.app.filestorage.LocalAssetDataLoader
import com.shoppinggenius.app.network.model.CategoryNetwork
import com.shoppinggenius.app.network.model.NetworkChangeList
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
