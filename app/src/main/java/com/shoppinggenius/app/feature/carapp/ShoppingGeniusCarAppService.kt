package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.data.product.ProductRepository
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

    @Inject
    lateinit var productRepository: ProductRepository

    override fun createHostValidator(): HostValidator =
        // Für Produktion: HostValidator.Builder(applicationContext).addAllowedHosts(...).build()
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session =
        ShoppingGeniusSession(groceryListRepository, groceryRepository, productRepository)
}

