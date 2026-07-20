package com.rendox.grocerygenius

import android.app.Application
import com.rendox.grocerygenius.locale.AppLocaleManager
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
    }
}