package com.rendox.shoppinggenius.data

import com.rendox.shoppinggenius.data.category.CategoryRepository
import com.rendox.shoppinggenius.data.category.CategoryRepositoryImpl
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.grocery.GroceryRepositoryImpl
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepository
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepositoryImpl
import com.rendox.shoppinggenius.data.icons.IconRepository
import com.rendox.shoppinggenius.data.icons.IconRepositoryImpl
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.data.product.ProductRepositoryImpl
import com.rendox.shoppinggenius.data.userpreferences.UserPreferencesRepository
import com.rendox.shoppinggenius.data.userpreferences.UserPreferencesRepositoryImpl
import com.rendox.shoppinggenius.data.util.ConnectivityManagerNetworkMonitor
import com.rendox.shoppinggenius.data.util.NetworkMonitor
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
