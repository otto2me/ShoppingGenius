package com.shoppinggenius.app.feature.carapp

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
import com.shoppinggenius.app.R
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.GroceryList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Zeigt alle Artikel einer Einkaufsliste mit visueller Gruppierung nach Status.
 * Jeder Artikel hat einen Toggle-Schalter zum Abhaken (purchased).
 */
class GroceryItemsScreen(
    carContext: CarContext,
    private val groceryList: GroceryList,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Screen(carContext) {

    private val iconLoader = CarAppIconLoader(
        filesDir = carContext.filesDir,
        coroutineScope = lifecycleScope,
        onIconLoaded = ::invalidate
    )

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
                iconLoader.preload(items.asSequence().map { it.icon }.asIterable())
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
            val openGroceries = groceries
                .asSequence()
                .filter { !it.purchased }
                .sortedWith(
                    compareBy<Grocery>(
                        { it.category?.sortingPriority ?: Long.MAX_VALUE },
                        { it.name.lowercase() }
                    )
                )
                .toList()
            val completedGroceries = groceries
                .asSequence()
                .filter { it.purchased }
                .sortedByDescending { it.purchasedLastModified }
                .toList()

            // Open items section with shopping icon
            if (openGroceries.isNotEmpty()) {
                addSectionHeader(listBuilder, "📝 " + carContext.getString(R.string.car_app_section_open))
                openGroceries.forEach { grocery -> addGroceryRow(listBuilder, grocery) }
            }
            
            // Completed items section with checkmark icon
            if (completedGroceries.isNotEmpty()) {
                addSectionHeader(listBuilder, "✅ " + carContext.getString(R.string.car_app_section_completed))
                completedGroceries.forEach { grocery -> addGroceryRow(listBuilder, grocery) }
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

    private fun addSectionHeader(
        listBuilder: ItemList.Builder,
        title: String
    ) {
        listBuilder.addItem(Row.Builder().setTitle(title).build())
    }

    private fun addGroceryRow(
        listBuilder: ItemList.Builder,
        grocery: Grocery
    ) {
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

        iconLoader.getOrLoad(grocery.icon)?.let { carIcon ->
            rowBuilder.setImage(carIcon, Row.IMAGE_TYPE_SMALL)
        }

        val subtitle = grocery.category?.name
            ?: grocery.description?.takeIf { it.isNotBlank() }
        subtitle?.let { rowBuilder.addText(it) }

        listBuilder.addItem(rowBuilder.build())
    }
}

