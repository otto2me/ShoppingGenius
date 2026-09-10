package com.shoppinggenius.app.feature.carapp

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.data.product.ProductRepository

class ShoppingGeniusSession(
    private val groceryListRepository: GroceryListRepository,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Session() {

    override fun onCreateScreen(intent: Intent): Screen =
        GroceryListsScreen(carContext, groceryListRepository, groceryRepository, productRepository)
}

