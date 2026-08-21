package com.rendox.shoppinggenius

import android.app.Application
import com.rendox.shoppinggenius.locale.AppLocaleManager
import com.rendox.shoppinggenius.sync.work.initializers.AutoDeleteCompletedGroceriesInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class ShoppingGeniusApp : Application() {
    @Inject
    lateinit var appLocaleManager: AppLocaleManager

    override fun onCreate() {
        super.onCreate()
        runBlocking(Dispatchers.IO) {
            appLocaleManager.applySavedLanguage()
        }
        AutoDeleteCompletedGroceriesInitializer.initialize(this)
    }
}
