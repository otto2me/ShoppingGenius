package com.shoppinggenius.app.sync.work.di

import com.shoppinggenius.app.data.util.SyncManager
import com.shoppinggenius.app.sync.work.status.WorkManagerSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    internal abstract fun bindsSyncStatusMonitor(syncStatusMonitor: WorkManagerSyncManager): SyncManager
}
