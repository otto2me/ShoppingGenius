package com.rendox.grocerygenius.sync.work.initializers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.rendox.grocerygenius.sync.work.workers.AutoDeleteCompletedGroceriesWorker

object AutoDeleteCompletedGroceriesInitializer {
    fun initialize(context: Context) {
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(
                AUTO_DELETE_COMPLETED_STARTUP_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                AutoDeleteCompletedGroceriesWorker.startUpCleanupWork()
            )
            enqueueUniquePeriodicWork(
                AUTO_DELETE_COMPLETED_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                AutoDeleteCompletedGroceriesWorker.periodicCleanupWork()
            )
        }
    }
}

private const val AUTO_DELETE_COMPLETED_STARTUP_WORK_NAME = "AutoDeleteCompletedStartupWork"
private const val AUTO_DELETE_COMPLETED_PERIODIC_WORK_NAME = "AutoDeleteCompletedPeriodicWork"

