package com.rendox.grocerygenius.feature.widget

import com.rendox.grocerygenius.database.grocery.GroceryDao
import com.rendox.grocerygenius.database.grocery.WidgetGroceryItem
import com.rendox.grocerygenius.database.grocerylist.GroceryListDao
import com.rendox.grocerygenius.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.first

internal data class ActiveWidgetListData(
    val listId: String?,
    val listName: String?,
    val backgroundOpacityPercent: Int,
    val items: List<WidgetGroceryItem>
)

internal suspend fun resolveActiveWidgetListData(
    userPreferencesDataSource: UserPreferencesDataSource,
    groceryListDao: GroceryListDao,
    groceryDao: GroceryDao
): ActiveWidgetListData {
    val preferences = userPreferencesDataSource.userPreferencesFlow.first()
    val backgroundOpacityPercent = preferences.widgetBackgroundOpacityPercent.coerceIn(0, 100)

    // "Aktive" Liste = zuletzt geoeffnete Liste; falls nicht vorhanden, auf Default/Fallback gehen.
    var listId = preferences.lastOpenedListId ?: preferences.defaultListId

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
            backgroundOpacityPercent = backgroundOpacityPercent,
            items = emptyList()
        )
    }

    val listName = groceryListDao.getGroceryListNameById(listId)
    val items = groceryDao.getGroceriesForWidget(listId)

    return ActiveWidgetListData(
        listId = listId,
        listName = listName,
        backgroundOpacityPercent = backgroundOpacityPercent,
        items = items
    )
}

