package com.shoppinggenius.app.data.grocery

import android.content.Context
import com.shoppinggenius.app.data.model.asExternalModel
import com.shoppinggenius.app.database.grocery.GroceryDao
import com.shoppinggenius.app.database.grocery.GroceryEntity
import com.shoppinggenius.app.database.grocerylist.GroceryListDao
import com.shoppinggenius.app.database.product.ProductDao
import com.shoppinggenius.app.database.product.ProductEntity
import com.shoppinggenius.app.feature.widget.ActiveGroceryListWidgetProvider
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.GroceryListType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GroceryRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val groceryDao: GroceryDao,
    private val groceryListDao: GroceryListDao,
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
        val listType = groceryListDao.getGroceryListById(groceryListId)
            .map { it?.type ?: GroceryListType.SHOPPING }
            .first()
        val productShouldBeShownInCatalog = listType == GroceryListType.SHOPPING
        val product = ProductEntity(
            id = productId,
            name = name,
            categoryId = categoryId,
            iconFileName = iconId,
            isDefault = isDefault,
            isFavorite = false,
            showInCatalog = productShouldBeShownInCatalog,
            ownerGroceryListId = groceryListId.takeUnless { productShouldBeShownInCatalog }
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
        productDao.deleteLocalOnlyProductIfOrphaned(productId)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun removeGroceriesFromList(listId: String) {
        groceryDao.deleteGroceriesFromList(listId)
        productDao.deleteOrphanedLocalOnlyProductsByOwnerListId(listId)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun deleteOldCompletedGroceries(beforeTimestampMs: Long): Int {
        val deletedCount = groceryDao.deleteOldCompletedGroceries(beforeTimestampMs)
        if (deletedCount > 0) {
            productDao.deleteAllOrphanedLocalOnlyProducts()
            ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
        }
        return deletedCount
    }
}
