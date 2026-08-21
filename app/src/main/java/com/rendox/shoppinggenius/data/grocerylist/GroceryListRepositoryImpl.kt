package com.rendox.shoppinggenius.data.grocerylist

import android.content.Context
import com.rendox.shoppinggenius.data.model.asEntity
import com.rendox.shoppinggenius.database.grocerylist.GroceryListDao
import com.rendox.shoppinggenius.feature.widget.ActiveGroceryListWidgetProvider
import com.rendox.shoppinggenius.model.GroceryList
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GroceryListRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val groceryListDao: GroceryListDao
) : GroceryListRepository {
    override suspend fun insertGroceryList(groceryList: GroceryList) {
        groceryListDao.insertGroceryList(groceryList.asEntity())
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override fun getGroceryListById(id: String): Flow<GroceryList?> {
        return groceryListDao.getGroceryListById(id)
    }

    override fun getAllGroceryLists(): Flow<List<GroceryList>> {
        return groceryListDao.getAllGroceryLists()
    }

    override suspend fun updateGroceryListName(
        listId: String,
        name: String
    ) {
        groceryListDao.updateGroceryListName(listId, name)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun deleteGroceryListById(groceryListId: String) {
        groceryListDao.deleteGroceryListById(groceryListId)
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun upsertGroceryLists(groceryLists: List<GroceryList>) {
        groceryListDao.upsertGroceryLists(groceryLists.map { it.asEntity() })
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    override suspend fun updateGroceryLists(groceryLists: List<GroceryList>) {
        groceryListDao.updateGroceryLists(groceryLists.map { it.asEntity() })
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }
}
