package com.rendox.grocerygenius.filestorage

import android.content.Context
import android.util.Log
import com.rendox.grocerygenius.model.AppLanguage
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
    suspend fun loadCategoriesJson(languageTag: String? = null): List<CategoryNetwork> =
        loadJsonListFromAssets(
            assetPath = categoriesAssetPath(languageTag),
            fallbackAssetPath = "category/categories_en.json",
            itemClass = CategoryNetwork::class.java,
            errorLabel = "categories"
        )

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

    suspend fun loadProductsJson(languageTag: String? = null): List<ProductNetwork> =
        loadJsonListFromAssets(
            assetPath = productsAssetPath(languageTag),
            fallbackAssetPath = "product/default_products_en.json",
            itemClass = ProductNetwork::class.java,
            errorLabel = "products"
        )

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

    private suspend fun <T> loadJsonListFromAssets(
        assetPath: String,
        fallbackAssetPath: String,
        itemClass: Class<T>,
        errorLabel: String
    ): List<T> = withContext(Dispatchers.IO) {
        try {
            readJsonList(assetPath, itemClass)
        } catch (e: Exception) {
            if (assetPath == fallbackAssetPath) {
                Log.e("LocalAssetDataLoader", "Error loading $errorLabel from $assetPath: ${e.message}")
                emptyList()
            } else {
                try {
                    readJsonList(fallbackAssetPath, itemClass)
                } catch (fallbackException: Exception) {
                    Log.e("LocalAssetDataLoader", "Error loading $errorLabel from $assetPath and fallback $fallbackAssetPath: ${fallbackException.message}")
                    emptyList()
                }
            }
        }
    }

    private fun <T> readJsonList(assetPath: String, itemClass: Class<T>): List<T> {
        val json = appContext.assets
            .open(assetPath)
            .bufferedReader()
            .use { it.readText() }
        val type = Types.newParameterizedType(List::class.java, itemClass)
        val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
        return adapter.fromJson(json) ?: emptyList()
    }

    private fun categoriesAssetPath(languageTag: String?): String {
        val resolvedLanguageTag = AppLanguage.resolveAssetLanguageTag(languageTag)
        return if (resolvedLanguageTag == "en") {
            "category/categories_en.json"
        } else {
            "category/${java.util.Locale.forLanguageTag(resolvedLanguageTag)}/categories.json"
        }
    }

    private fun productsAssetPath(languageTag: String?): String {
        val resolvedLanguageTag = AppLanguage.resolveAssetLanguageTag(languageTag)
        return if (resolvedLanguageTag == "en") {
            "product/default_products_en.json"
        } else {
            "product/${java.util.Locale.forLanguageTag(resolvedLanguageTag)}/default_products.json"
        }
    }
}

