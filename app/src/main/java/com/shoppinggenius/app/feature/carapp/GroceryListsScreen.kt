package com.shoppinggenius.app.feature.carapp

import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.RequiresCarApi
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
import com.shoppinggenius.app.model.GroceryListType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

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
        // Initialisiere Speed-Listener für Google Driver Distraction Guidelines
        // Dies wird auf dem Main Thread ausgeführt, um sicherzustellen, dass der Listener
        // im korrekten Thread-Kontext registriert wird
        initializeSpeedListener()

        lifecycleScope.launch {
            groceryListRepository.getAllGroceryLists().collectLatest { lists ->
                groceryLists = lists
                isLoading = false
                invalidate()
            }
        }
    }

    @RequiresCarApi(3)
    private fun initializeSpeedListener() {
        try {
            // Erstelle einen Executor, der auf dem Main Thread ausgeführt wird
            val mainThreadExecutor = Executor { runnable ->
                Handler(Looper.getMainLooper()).post(runnable)
            }
            CarAppDrivingStateHelper.initializeSpeedListener(carContext, mainThreadExecutor)
        } catch (e: Exception) {
            // Speed-Listener nicht verfügbar (z.B. auf älterer API)
            e.printStackTrace()
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
        
        listBuilder.addItem(
            Row.Builder()
                .setTitle(list.name)
                .addText(itemCountText)
                .setOnClickListener {
                    screenManager.push(
                        GroceryItemsScreen(carContext, list, groceryRepository, productRepository)
                    )
                }
                .build()
        )
    }
}

