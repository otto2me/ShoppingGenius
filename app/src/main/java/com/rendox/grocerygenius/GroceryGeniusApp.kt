package com.rendox.grocerygenius

import android.app.Application
import com.rendox.grocerygenius.locale.AppLocaleManager
import com.rendox.grocerygenius.sync.work.initializers.Sync
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class GroceryGeniusApp : Application() {
    @Inject
    lateinit var appLocaleManager: AppLocaleManager

    override fun onCreate() {
        super.onCreate()
        runBlocking(Dispatchers.IO) {
            appLocaleManager.applySavedLanguage()
        }
        // Initialize Sync; the system responsible for keeping data in the app up to date.
        Sync.initialize(context = this)
    }
}