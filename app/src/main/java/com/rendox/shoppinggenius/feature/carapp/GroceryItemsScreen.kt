package com.rendox.shoppinggenius.feature.carapp

import android.graphics.BitmapFactory
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.GroceryList
import com.rendox.shoppinggenius.model.IconReference
import java.io.File
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

            if (openGroceries.isNotEmpty()) {
                addSectionHeader(listBuilder, carContext.getString(R.string.car_app_section_open))
                openGroceries.forEach { grocery -> addGroceryRow(listBuilder, grocery) }
            }
            if (completedGroceries.isNotEmpty()) {
                addSectionHeader(listBuilder, carContext.getString(R.string.car_app_section_completed))
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

        toCarIcon(grocery.icon)?.let { carIcon ->
            rowBuilder.setImage(carIcon, Row.IMAGE_TYPE_SMALL)
        }

        val subtitle = grocery.category?.name
            ?: grocery.description?.takeIf { it.isNotBlank() }
        subtitle?.let { rowBuilder.addText(it) }

        listBuilder.addItem(rowBuilder.build())
    }

    private fun toCarIcon(iconReference: IconReference?): CarIcon? {
        val filePath = iconReference?.filePath ?: return null
        val iconFile = File(carContext.filesDir, filePath)
        if (!iconFile.exists()) return null

        val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath) ?: return null
        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
    }
}

