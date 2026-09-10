package com.shoppinggenius.app.data

import com.shoppinggenius.app.data.category.CategoryRepository
import com.shoppinggenius.app.data.category.CategoryRepositoryImpl
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.grocery.GroceryRepositoryImpl
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.data.grocerylist.GroceryListRepositoryImpl
import com.shoppinggenius.app.data.icons.IconRepository
import com.shoppinggenius.app.data.icons.IconRepositoryImpl
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.data.product.ProductRepositoryImpl
import com.shoppinggenius.app.data.userpreferences.UserPreferencesRepository
import com.shoppinggenius.app.data.userpreferences.UserPreferencesRepositoryImpl
import com.shoppinggenius.app.data.util.ConnectivityManagerNetworkMonitor
import com.shoppinggenius.app.data.util.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun groceryRepository(groceryRepositoryImpl: GroceryRepositoryImpl): GroceryRepository

    @Binds
    @Singleton
    abstract fun bindGroceryListRepository(groceryListRepositoryImpl: GroceryListRepositoryImpl): GroceryListRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindIconRepository(iconRepositoryImpl: IconRepositoryImpl): IconRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(networkMonitor: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
