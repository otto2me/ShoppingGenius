package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.shoppinggenius.app.R
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.GroceryList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Zeigt alle Artikel einer Einkaufsliste mit visueller Gruppierung nach Status.
 * Jeder Artikel kann per geparkter-only Aktion als erledigt/offen markiert werden.
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

    private val restrictionsHelper = CarUxRestrictionsHelper(carContext)
    private var drivingState = restrictionsHelper.getDrivingState()

    private val addItemAction: Action
        get() {
            val action = Action.Builder()
                .setTitle(carContext.getString(R.string.car_app_add_item))

            action.setOnClickListener {
                screenManager.push(
                    ProductPickerScreen(
                        carContext = carContext,
                        groceryList = groceryList,
                        groceryRepository = groceryRepository,
                        productRepository = productRepository
                    )
                )
            }

            return action.build()
        }

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
        lifecycleScope.launch {
            while (true) {
                val updatedDrivingState = restrictionsHelper.getDrivingState()
                if (updatedDrivingState != drivingState) {
                    drivingState = updatedDrivingState
                    invalidate()
                }
                delay(RESTRICTION_POLL_INTERVAL)
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
        val isDriverDistracted = drivingState == CarUxRestrictionsHelper.DrivingState.RESTRICTED
        val rowBuilder = Row.Builder()
            .setTitle(grocery.name)

        val parkedOnlyActionLabel = if (grocery.purchased) {
            carContext.getString(R.string.car_app_mark_open)
        } else {
            carContext.getString(R.string.car_app_mark_completed)
        }

        rowBuilder.setOnClickListener(
            ParkedOnlyOnClickListener.create {
                lifecycleScope.launch {
                    groceryRepository.updatePurchased(
                        productId = grocery.productId,
                        listId = groceryList.id,
                        purchased = !grocery.purchased
                    )
                }
            }
        )

        if (isDriverDistracted) {
            rowBuilder.addText(carContext.getString(R.string.car_app_parked_only_hint))
        } else {
            rowBuilder.addText(parkedOnlyActionLabel)
        }
        iconLoader.getOrLoad(grocery.icon)?.let { carIcon ->
            rowBuilder.setImage(carIcon, Row.IMAGE_TYPE_SMALL)
        }

        val subtitle = grocery.category?.name
            ?: grocery.description?.takeIf { it.isNotBlank() }
        subtitle?.let { rowBuilder.addText(it) }

        listBuilder.addItem(rowBuilder.build())
    }

    private companion object {
        val RESTRICTION_POLL_INTERVAL = 1.seconds
    }
}

