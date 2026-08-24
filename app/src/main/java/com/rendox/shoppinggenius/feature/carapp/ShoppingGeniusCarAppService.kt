package com.rendox.shoppinggenius.feature.carapp

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Einstiegspunkt der App für Android Auto und Android Automotive OS.
 * Nutzt Hilt-Injection für den Zugriff auf die Repositories.
 */
@AndroidEntryPoint
class ShoppingGeniusCarAppService : CarAppService() {

    @Inject
    lateinit var groceryListRepository: GroceryListRepository

    @Inject
    lateinit var groceryRepository: GroceryRepository

    override fun createHostValidator(): HostValidator =
        // Für Produktion: HostValidator.Builder(applicationContext).addAllowedHosts(...).build()
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session =
        ShoppingGeniusSession(groceryListRepository, groceryRepository)
}

