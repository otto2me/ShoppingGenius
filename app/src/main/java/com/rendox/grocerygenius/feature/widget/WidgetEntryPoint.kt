package com.rendox.grocerygenius.feature.widget

import com.rendox.grocerygenius.database.grocery.GroceryDao
import com.rendox.grocerygenius.database.grocerylist.GroceryListDao
import com.rendox.grocerygenius.datastore.UserPreferencesDataSource
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

