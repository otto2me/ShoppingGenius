package com.rendox.grocerygenius.feature.widget

import com.rendox.grocerygenius.database.grocery.GroceryDao
import com.rendox.grocerygenius.database.grocery.WidgetGroceryItem
import com.rendox.grocerygenius.database.grocerylist.GroceryListDao
import com.rendox.grocerygenius.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.first

internal data class ActiveWidgetListData(
    val listId: String?,
    val listName: String?,
    val items: List<WidgetGroceryItem>
)

internal suspend fun resolveActiveWidgetListData(
    userPreferencesDataSource: UserPreferencesDataSource,
    groceryListDao: GroceryListDao,
    groceryDao: GroceryDao
): ActiveWidgetListData {
    val preferences = userPreferencesDataSource.userPreferencesFlow.first()

    var listId = if (preferences.openLastViewedList) {
        preferences.lastOpenedListId
    } else {
        preferences.defaultListId
    }

    if (listId != null && groceryListDao.getGroceryListNameById(listId) == null) {
        listId = null
    }

    if (listId == null) {
        listId = groceryListDao.getFirstGroceryListId()
    }

    if (listId == null) {
        return ActiveWidgetListData(
            listId = null,
            listName = null,
            items = emptyList()
        )
    }

    val listName = groceryListDao.getGroceryListNameById(listId)
    val items = groceryDao.getGroceriesForWidget(listId)

    return ActiveWidgetListData(
        listId = listId,
        listName = listName,
        items = items
    )
}

