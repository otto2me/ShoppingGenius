package com.rendox.shoppinggenius.network.di

import com.rendox.shoppinggenius.network.data.sources.category.CategoryNetworkDataSource
import com.rendox.shoppinggenius.network.data.sources.category.OfflineFirstCategoryNetworkDataSource
import com.rendox.shoppinggenius.network.data.sources.icon.IconNetworkDataSource
import com.rendox.shoppinggenius.network.data.sources.icon.OfflineFirstIconNetworkDataSource
import com.rendox.shoppinggenius.network.data.sources.product.OfflineFirstProductNetworkDataSource
import com.rendox.shoppinggenius.network.data.sources.product.ProductNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindCategoryNetworkDataSource(
        categoryNetworkDataSource: OfflineFirstCategoryNetworkDataSource
    ): CategoryNetworkDataSource

    @Binds
    @Singleton
    abstract fun bindProductNetworkDataSource(
        productNetworkDataSource: OfflineFirstProductNetworkDataSource
    ): ProductNetworkDataSource

    @Binds
    @Singleton
    abstract fun bindIconNetworkDataSource(
        iconNetworkDataSource: OfflineFirstIconNetworkDataSource
    ): IconNetworkDataSource
}
