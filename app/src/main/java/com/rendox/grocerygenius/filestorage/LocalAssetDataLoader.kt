package com.rendox.grocerygenius.filestorage

import android.content.Context
import android.util.Log
import com.rendox.grocerygenius.model.IconReference
import com.rendox.grocerygenius.network.model.CategoryNetwork
import com.rendox.grocerygenius.network.model.NetworkChangeList
import com.rendox.grocerygenius.network.model.ProductNetwork
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAssetDataLoader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val moshi: Moshi
) {
    suspend fun loadCategoriesJson(languageCode: String = "en"): List<CategoryNetwork> = withContext(Dispatchers.IO) {
        try {
            val fileName = "category/categories_${languageCode}.json"
            val json = appContext.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, CategoryNetwork::class.java)
            val adapter: JsonAdapter<List<CategoryNetwork>> = moshi.adapter(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading categories for language $languageCode: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadCategoriesChangeList(): List<NetworkChangeList> = withContext(Dispatchers.IO) {
        try {
            val json = appContext.assets
                .open("category/categories_change_list.json")
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, NetworkChangeList::class.java)
            val adapter: JsonAdapter<List<NetworkChangeList>> = moshi.adapter(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading categories change list: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadProductsJson(languageCode: String = "en"): List<ProductNetwork> = withContext(Dispatchers.IO) {
        try {
            val fileName = "product/default_products_${languageCode}.json"
            val json = appContext.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, ProductNetwork::class.java)
            val adapter: JsonAdapter<List<ProductNetwork>> = moshi.adapter(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading products for language $languageCode: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadProductsChangeList(): List<NetworkChangeList> = withContext(Dispatchers.IO) {
        try {
            val json = appContext.assets
                .open("product/default_products_change_list.json")
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, NetworkChangeList::class.java)
            val adapter: JsonAdapter<List<NetworkChangeList>> = moshi.adapter(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading products change list: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadIconsChangeList(): List<NetworkChangeList> = withContext(Dispatchers.IO) {
        try {
            val json = appContext.assets
                .open("icons/icons_change_list.json")
                .bufferedReader()
                .use { it.readText() }

            val type = Types.newParameterizedType(List::class.java, NetworkChangeList::class.java)
            val adapter: JsonAdapter<List<NetworkChangeList>> = moshi.adapter(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading icons change list: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadIconsFromAssets(): List<IconReference> = withContext(Dispatchers.IO) {
        try {
            val iconFiles = appContext.assets.list("icons")
                ?.filter {
                    it.endsWith(".png") && !it.startsWith("all_icons")
                } ?: emptyList()

            iconFiles.map { fileName ->
                IconReference(
                    uniqueFileName = fileName,
                    filePath = "icons/$fileName"
                )
            }
        } catch (e: Exception) {
            Log.e("LocalAssetDataLoader", "Error loading icons from assets: ${e.message}")
            emptyList()
        }
    }
}

