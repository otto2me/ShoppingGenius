package com.rendox.shoppinggenius.sync.work.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.rendox.shoppinggenius.data.grocery.CompletedGroceriesAutoDeletePolicy
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.userpreferences.UserPreferencesRepository
import com.rendox.shoppinggenius.network.di.Dispatcher
import com.rendox.shoppinggenius.network.di.ShoppingGeniusDispatchers
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class AutoDeleteCompletedGroceriesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val groceryRepository: GroceryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Dispatcher(ShoppingGeniusDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        val autoDeleteAfterHours =
            userPreferencesRepository.userPreferencesFlow.first().autoDeleteCompletedAfterHours
        val beforeTimestampMs = CompletedGroceriesAutoDeletePolicy.cutoffTimestampMs(
            nowMs = System.currentTimeMillis(),
            autoDeleteAfterHours = autoDeleteAfterHours
        )
        groceryRepository.deleteOldCompletedGroceries(beforeTimestampMs)
        Result.success()
    }

    companion object {
        private const val REPEAT_INTERVAL_MINUTES = 15L

        fun startUpCleanupWork(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DelegatingWorker>()
                .setInputData(AutoDeleteCompletedGroceriesWorker::class.delegatedData())
                .build()
        }

        fun periodicCleanupWork(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<DelegatingWorker>(
                REPEAT_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setInputData(AutoDeleteCompletedGroceriesWorker::class.delegatedData())
                .build()
        }
    }
}


