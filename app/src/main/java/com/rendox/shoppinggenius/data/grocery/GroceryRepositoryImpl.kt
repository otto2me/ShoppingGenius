package com.rendox.shoppinggenius.data.grocery

import android.content.Context
import com.rendox.shoppinggenius.data.model.asExternalModel
import com.rendox.shoppinggenius.database.grocery.GroceryDao
import com.rendox.shoppinggenius.database.grocery.GroceryEntity
import com.rendox.shoppinggenius.database.product.ProductDao
import com.rendox.shoppinggenius.database.product.ProductEntity
import com.rendox.shoppinggenius.feature.widget.ActiveGroceryListWidgetProvider
import com.rendox.shoppinggenius.model.Grocery
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GroceryRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val groceryDao: GroceryDao,
    private val productDao: ProductDao
) : GroceryRepository {
    override suspend fun addGroceryToList(
        productId: String,
        listId: String,
        description: String?,
        purchased: Boolean,
        purchasedLastModified: Long
    ) {
        groceryDao.insertGrocery(
            GroceryEntity(
                productId = productId,
                groceryListId = listId,
                description = description,
                purchased = purchased,
                purchasedLastModified = purchasedLastModified
            )
        )
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun insertProductAndGrocery(
        name: String,
        productId: String,
        iconId: String?,
        categoryId: String?,
        groceryListId: String,
        description: String?,
        purchased: Boolean,
        purchasedLastModified: Long,
        isDefault: Boolean
    ) {
        val product = ProductEntity(
            id = productId,
            name = name,
            categoryId = categoryId,
            iconFileName = iconId,
            isDefault = isDefault,
            isFavorite = false
        )
        val grocery = GroceryEntity(
            productId = productId,
            groceryListId = groceryListId,
            description = description,
            purchased = purchased,
            purchasedLastModified = purchasedLastModified
        )
        productDao.insertProduct(product)
        groceryDao.insertGrocery(grocery)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override fun getGroceriesFromList(listId: String): Flow<List<Grocery>> {
        return groceryDao.getGroceriesFromList(listId).map { combinedGroceries ->
            combinedGroceries.map { combinedGrocery ->
                combinedGrocery.asExternalModel()
            }
        }
    }

    override fun getGroceryById(
        productId: String,
        listId: String
    ): Flow<Grocery?> {
        return groceryDao.getGrocery(productId, listId).map { it?.asExternalModel() }
    }

    override suspend fun updatePurchased(
        productId: String,
        listId: String,
        purchased: Boolean,
        purchasedLastModified: Long
    ) {
        groceryDao.updatePurchased(
            productId,
            listId,
            purchased,
            purchasedLastModified
        )
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun updateDescription(
        productId: String,
        listId: String,
        description: String?
    ) {
        groceryDao.updateDescription(productId, listId, description)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun removeGroceryFromList(
        productId: String,
        listId: String
    ) {
        groceryDao.deleteGrocery(productId, listId)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun removeGroceriesFromList(listId: String) {
        groceryDao.deleteGroceriesFromList(listId)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun deleteOldCompletedGroceries(beforeTimestampMs: Long): Int {
        val deletedCount = groceryDao.deleteOldCompletedGroceries(beforeTimestampMs)
        if (deletedCount > 0) {
            ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
        }
        return deletedCount
    }
}
