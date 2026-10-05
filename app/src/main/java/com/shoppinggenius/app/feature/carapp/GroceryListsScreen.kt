package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.shoppinggenius.app.R
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.GroceryListType
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
    private val restrictionsHelper = CarUxRestrictionsHelper(carContext)

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
            // Separate shopping and todo lists for better visual organization
            val shoppingLists = groceryLists.filter { it.type == GroceryListType.SHOPPING }
            val todoLists = groceryLists.filter { it.type == GroceryListType.TODO }

            // Add shopping lists with icon
            if (shoppingLists.isNotEmpty()) {
                if (todoLists.isNotEmpty()) {
                    addSectionHeader(listBuilder, "🛒 " + carContext.getString(R.string.grocery_list_type_shopping))
                }
                shoppingLists.forEach { list ->
                    addListRow(listBuilder, list)
                }
            }

            // Add todo lists with icon
            if (todoLists.isNotEmpty()) {
                if (shoppingLists.isNotEmpty()) {
                    addSectionHeader(listBuilder, "✓ " + carContext.getString(R.string.grocery_list_type_todo))
                }
                todoLists.forEach { list ->
                    addListRow(listBuilder, list)
                }
            }
        }

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(listBuilder.build())
            .setLoading(false)
            .build()
    }

    private fun addSectionHeader(
        listBuilder: ItemList.Builder,
        title: String
    ) {
        listBuilder.addItem(Row.Builder().setTitle(title).build())
    }

    private fun addListRow(
        listBuilder: ItemList.Builder,
        list: GroceryList
    ) {
        val itemCountText = carContext.getString(R.string.car_app_item_count, list.numOfGroceries)
        val rowBuilder = Row.Builder()
            .setTitle(list.name)
            .addText(itemCountText)

        rowBuilder.setBrowsable(true)
        rowBuilder.setOnClickListener(
            ParkedOnlyOnClickListener.create {
                screenManager.push(
                    GroceryItemsScreen(carContext, list, groceryRepository, productRepository)
                )
            }
        )

        if (restrictionsHelper.isDriverDistracted()) {
            rowBuilder.addText(carContext.getString(R.string.car_app_voice_only_hint))
        }

        listBuilder.addItem(rowBuilder.build())
    }
}

