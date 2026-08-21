package com.rendox.shoppinggenius.database

import com.rendox.shoppinggenius.database.category.CategoryDao
import com.rendox.shoppinggenius.database.grocery.GroceryDao
import com.rendox.shoppinggenius.database.groceryicon.IconDao
import com.rendox.shoppinggenius.database.grocerylist.GroceryListDao
import com.rendox.shoppinggenius.database.product.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun providesCategoryDao(appDatabase: AppDatabase): CategoryDao = appDatabase.categoryDao()

    @Provides
    fun providesGroceryDao(appDatabase: AppDatabase): GroceryDao = appDatabase.groceryDao()

    @Provides
    fun providesGroceryListDao(appDatabase: AppDatabase): GroceryListDao = appDatabase.groceryListDao()

    @Provides
    fun providesProductDao(appDatabase: AppDatabase): ProductDao = appDatabase.productDao()

    @Provides
    fun providesIconDao(appDatabase: AppDatabase): IconDao = appDatabase.iconDao()
}
