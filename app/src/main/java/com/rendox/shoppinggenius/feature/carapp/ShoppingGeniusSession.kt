package com.rendox.shoppinggenius.feature.carapp

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepository
import com.rendox.shoppinggenius.data.product.ProductRepository

class ShoppingGeniusSession(
    private val groceryListRepository: GroceryListRepository,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Session() {

    override fun onCreateScreen(intent: Intent): Screen =
        GroceryListsScreen(carContext, groceryListRepository, groceryRepository, productRepository)
}

