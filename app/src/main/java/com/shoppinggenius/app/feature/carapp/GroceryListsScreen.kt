package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.shoppinggenius.app.R
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.GroceryList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Zeigt alle Einkaufslisten als scrollbare Liste im Auto-Bildschirm.
 * Tippen auf eine Liste öffnet die enthaltenen Artikel.
 */
class GroceryListsScreen(
    carContext: CarContext,
    private val groceryListRepository: GroceryListRepository,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Screen(carContext) {

    private var groceryLists: List<GroceryList> = emptyList()
    private var isLoading = true

    init {
        lifecycleScope.launch {
            groceryListRepository.getAllGroceryLists().collectLatest { lists ->
                groceryLists = lists
                isLoading = false
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(carContext.getString(R.string.app_name))
                .setHeaderAction(Action.APP_ICON)
                .setLoading(true)
                .build()
        }

        val listBuilder = ItemList.Builder()

        if (groceryLists.isEmpty()) {
            listBuilder.setNoItemsMessage(carContext.getString(R.string.car_app_no_lists))
        } else {
            groceryLists.forEach { list ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(list.name)
                        .addText(
                            carContext.getString(R.string.car_app_item_count, list.numOfGroceries)
                        )
                        .setOnClickListener {
                            screenManager.push(
                                GroceryItemsScreen(carContext, list, groceryRepository, productRepository)
                            )
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(listBuilder.build())
            .setLoading(false)
            .build()
    }
}

