package com.rendox.shoppinggenius.feature.widget

import com.rendox.shoppinggenius.database.grocery.GroceryDao
import com.rendox.shoppinggenius.database.grocerylist.GroceryListDao
import com.rendox.shoppinggenius.datastore.UserPreferencesDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun groceryDao(): GroceryDao
    fun groceryListDao(): GroceryListDao
    fun userPreferencesDataSource(): UserPreferencesDataSource
}


