package com.rendox.grocerygenius.data.product

import com.rendox.grocerygenius.data.Syncable
import com.rendox.grocerygenius.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository : Syncable {
    suspend fun insertProduct(product: Product)
    fun getProductById(productId: String): Flow<Product?>
    fun getAllProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String?): Flow<List<Product>>
    suspend fun getProductsByName(name: String): List<Product>
    suspend fun getProductsByKeywords(keywords: List<String>): List<Product>
    suspend fun updateProductName(
        productId: String,
        name: String
    )
    suspend fun updateProductCategory(
        productId: String,
        categoryId: String?
    )
    suspend fun updateProductIcon(
        productId: String,
        iconId: String?
    )
    suspend fun updateProductFavorite(
        productId: String,
        isFavorite: Boolean
    )
    suspend fun deleteProductById(productId: String)
}