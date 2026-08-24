package com.rendox.shoppinggenius.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepository
import com.rendox.shoppinggenius.model.GroceryList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Zeigt alle Einkaufslisten als scrollbare Liste im Auto-Bildschirm.
 * Tippen auf eine Liste öffnet die enthaltenen Artikel.
 */
class GroceryListsScreen(
    carContext: CarContext,
    private val groceryListRepository: GroceryListRepository,
    private val groceryRepository: GroceryRepository
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
        val listBuilder = ItemList.Builder()

        if (!isLoading) {
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
                                    GroceryItemsScreen(carContext, list, groceryRepository)
                                )
                            }
                            .build()
                    )
                }
            }
        }

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(listBuilder.build())
            .setLoading(isLoading)
            .build()
    }
}

