package com.rendox.shoppinggenius.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle
import androidx.lifecycle.lifecycleScope
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.GroceryList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Zeigt alle Artikel einer Einkaufsliste.
 * Jeder Artikel hat einen Toggle-Schalter zum Abhaken (purchased).
 */
class GroceryItemsScreen(
    carContext: CarContext,
    private val groceryList: GroceryList,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Screen(carContext) {

    private val addItemAction = Action.Builder()
        .setTitle(carContext.getString(R.string.car_app_add_item))
        .setOnClickListener {
            screenManager.push(
                ProductPickerScreen(
                    carContext = carContext,
                    groceryList = groceryList,
                    groceryRepository = groceryRepository,
                    productRepository = productRepository
                )
            )
        }
        .build()

    private var groceries: List<Grocery> = emptyList()
    private var isLoading = true

    init {
        lifecycleScope.launch {
            groceryRepository.getGroceriesFromList(groceryList.id).collectLatest { items ->
                groceries = items
                isLoading = false
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(groceryList.name)
                .setHeaderAction(Action.BACK)
                .setActionStrip(ActionStrip.Builder().addAction(addItemAction).build())
                .setLoading(true)
                .build()
        }

        val listBuilder = ItemList.Builder()

        if (groceries.isEmpty()) {
            listBuilder.setNoItemsMessage(carContext.getString(R.string.car_app_no_items))
        } else {
            groceries.forEach { grocery ->
                val rowBuilder = Row.Builder()
                    .setTitle(grocery.name)
                    .setToggle(
                        Toggle.Builder { isChecked ->
                            lifecycleScope.launch {
                                groceryRepository.updatePurchased(
                                    productId = grocery.productId,
                                    listId = groceryList.id,
                                    purchased = isChecked
                                )
                            }
                        }
                        .setChecked(grocery.purchased)
                        .build()
                    )

                // Kategorie oder Beschreibung als Untertitel (max. 1 Zeile)
                val subtitle = grocery.category?.name
                    ?: grocery.description?.takeIf { it.isNotBlank() }
                subtitle?.let { rowBuilder.addText(it) }

                listBuilder.addItem(rowBuilder.build())
            }
        }

        return ListTemplate.Builder()
            .setTitle(groceryList.name)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .setActionStrip(ActionStrip.Builder().addAction(addItemAction).build())
            .setLoading(false)
            .build()
    }
}

