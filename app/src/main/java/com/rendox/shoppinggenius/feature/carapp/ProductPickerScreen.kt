package com.rendox.shoppinggenius.feature.carapp

import android.graphics.BitmapFactory
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.model.GroceryList
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.model.Product
import java.io.File
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Einfache Produktauswahl für Android Auto: Produkt antippen -> zur Einkaufsliste hinzufügen.
 */
class ProductPickerScreen(
    carContext: CarContext,
    private val groceryList: GroceryList,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : Screen(carContext) {

    private var allProducts: List<Product> = emptyList()
    private var isLoading = true
    private var searchQuery: String = ""

    init {
        lifecycleScope.launch {
            productRepository.getAllProducts().collectLatest { allProducts ->
                this@ProductPickerScreen.allProducts = allProducts
                    .sortedBy { it.name.lowercase() }
                isLoading = false
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val searchCallback = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                searchQuery = searchText
                invalidate()
            }

            override fun onSearchSubmitted(searchText: String) {
                val submittedText = searchText.trim()
                searchQuery = submittedText
                if (submittedText.isBlank()) {
                    invalidate()
                    return
                }

                val exactProduct = allProducts.firstOrNull {
                    it.name.equals(submittedText, ignoreCase = true)
                }
                if (exactProduct != null) {
                    addExistingProduct(exactProduct)
                } else {
                    addCustomProduct(submittedText)
                }
            }
        }

        if (isLoading) {
            return SearchTemplate.Builder(searchCallback)
                .setHeaderAction(Action.BACK)
                .setSearchHint(carContext.getString(R.string.car_app_search_hint))
                .setLoading(true)
                .build()
        }

        val listBuilder = ItemList.Builder()
        val query = searchQuery.trim()
        val hasPerfectMatch = query.isNotBlank() && allProducts.any {
            it.name.equals(query, ignoreCase = true)
        }
        val products = if (query.isBlank()) {
            allProducts.take(MAX_PRODUCTS)
        } else {
            allProducts
                .asSequence()
                .filter { it.name.contains(query, ignoreCase = true) }
                .take(MAX_PRODUCTS)
                .toList()
        }

        var hasRows = false
        if (query.isNotBlank() && !hasPerfectMatch) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.car_app_add_unknown_item, query))
                    .setOnClickListener {
                        addCustomProduct(query)
                    }
                    .build()
            )
            hasRows = true
        }

        if (products.isEmpty() && !hasRows) {
            val noItemsMessage = if (query.isBlank()) {
                carContext.getString(R.string.car_app_search_hint)
            } else {
                carContext.getString(R.string.car_app_no_products)
            }
            listBuilder.setNoItemsMessage(noItemsMessage)
        } else {
            products.forEach { product ->
                listBuilder.addItem(
                    Row.Builder().apply {
                        setTitle(product.name)
                        toCarIcon(product.icon)?.let { carIcon ->
                            setImage(carIcon, Row.IMAGE_TYPE_SMALL)
                        }
                        setOnClickListener {
                            addExistingProduct(product)
                        }
                    }.build()
                )
                hasRows = true
            }
        }

        return SearchTemplate.Builder(searchCallback)
            .setHeaderAction(Action.BACK)
            .setSearchHint(carContext.getString(R.string.car_app_search_hint))
            .setShowKeyboardByDefault(true)
            .setItemList(listBuilder.build())
            .setLoading(false)
            .build()
    }

    private fun addExistingProduct(product: Product) {
        lifecycleScope.launch {
            groceryRepository.addGroceryToList(
                productId = product.id,
                listId = groceryList.id,
                purchased = false
            )
            screenManager.pop()
        }
    }

    private fun addCustomProduct(name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        lifecycleScope.launch {
            groceryRepository.insertProductAndGrocery(
                name = normalizedName,
                categoryId = null,
                groceryListId = groceryList.id,
                description = null,
                purchased = false
            )
            screenManager.pop()
        }
    }

    private fun toCarIcon(iconReference: IconReference?): CarIcon? {
        val filePath = iconReference?.filePath ?: return null
        val iconFile = File(carContext.filesDir, filePath)
        if (!iconFile.exists()) return null

        val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath) ?: return null
        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
    }

    private companion object {
        const val MAX_PRODUCTS = 120
    }
}

