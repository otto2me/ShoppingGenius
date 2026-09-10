package com.shoppinggenius.app.database

import com.shoppinggenius.app.database.category.CategoryDao
import com.shoppinggenius.app.database.grocery.GroceryDao
import com.shoppinggenius.app.database.groceryicon.IconDao
import com.shoppinggenius.app.database.grocerylist.GroceryListDao
import com.shoppinggenius.app.database.product.ProductDao
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
