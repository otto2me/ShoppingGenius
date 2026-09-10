package com.shoppinggenius.app.feature.widget

import com.shoppinggenius.app.database.grocery.GroceryDao
import com.shoppinggenius.app.database.grocerylist.GroceryListDao
import com.shoppinggenius.app.datastore.UserPreferencesDataSource
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


