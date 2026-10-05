package com.shoppinggenius.app.feature.carapp

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.shoppinggenius.app.R
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.GroceryListType
import com.shoppinggenius.app.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Einfache Produktauswahl für Android Auto: Produkt antippen -> zur Einkaufsliste hinzufügen.
 */
class ProductPickerScreen(
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

    private var allProducts: List<Product> = emptyList()
    private var groceries: List<Grocery> = emptyList()
    private var areProductsLoading = true
    private var areGroceriesLoading = true
    private var searchQuery: String = ""
    private var isAddingItem = false

    private val isLoading: Boolean
        get() = areProductsLoading || areGroceriesLoading

    init {
        lifecycleScope.launch {
            // Only load products for shopping lists, not for todo lists
            if (groceryList.type == GroceryListType.SHOPPING) {
                productRepository.getAllProducts().collectLatest { allProducts ->
                    this@ProductPickerScreen.allProducts = allProducts
                        .sortedBy { it.name.lowercase() }
                    areProductsLoading = false
                    invalidate()
                }
            } else {
                // For TODO lists, skip product loading
                areProductsLoading = false
                invalidate()
            }
        }
        lifecycleScope.launch {
            groceryRepository.getGroceriesFromList(groceryList.id).collectLatest { groceries ->
                this@ProductPickerScreen.groceries = groceries
                areGroceriesLoading = false
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
        val isDriverDistracted = drivingState == CarUxRestrictionsHelper.DrivingState.RESTRICTED
        val searchCallback = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                searchQuery = searchText
                invalidate()
            }

            override fun onSearchSubmitted(searchText: String) {
                // Prevent duplicate submissions when voice recognition sends partial results
                if (isAddingItem) return

                val submittedText = searchText.trim()
                searchQuery = submittedText
                if (submittedText.isBlank()) {
                    invalidate()
                    return
                }

                if (isDuplicateGroceryName(submittedText, groceries)) {
                    invalidate()
                    return
                }

                // For shopping lists, try to find an exact product match
                // For todo lists, always add as custom product
                if (groceryList.type == GroceryListType.SHOPPING) {
                    val exactProduct = availableProductsForCarApp(allProducts, groceries).firstOrNull {
                        normalizeGroceryName(it.name) == normalizeGroceryName(submittedText)
                    }
                    if (exactProduct != null) {
                        addExistingProduct(exactProduct)
                    } else {
                        addCustomProduct(submittedText)
                    }
                } else {
                    // For todo lists, always add as custom product
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
        val queryNormalized = normalizeGroceryName(query)
        val queryAlreadyInList = queryNormalized.isNotEmpty() && isDuplicateGroceryName(query, groceries)

        // Only show predefined products for shopping lists, not for todo lists
        val isShoppingList = groceryList.type == GroceryListType.SHOPPING
        val availableProducts = if (isShoppingList) {
            availableProductsForCarApp(allProducts, groceries)
        } else {
            emptyList()
        }
        val hasPerfectMatch = queryNormalized.isNotEmpty() && availableProducts.any {
            normalizeGroceryName(it.name) == queryNormalized
        }
        val products = if (query.isBlank()) {
            availableProducts.take(MAX_PRODUCTS)
        } else {
            availableProducts
                .asSequence()
                .filter { it.name.contains(query, ignoreCase = true) }
                .take(MAX_PRODUCTS)
                .toList()
        }
        iconLoader.preload(products.asSequence().map { it.icon }.asIterable())

        var hasRows = false
        if (query.isNotBlank() && !hasPerfectMatch && !queryAlreadyInList) {
            val rowBuilder = Row.Builder()
                .setTitle(carContext.getString(R.string.car_app_add_unknown_item, query))

            rowBuilder.setOnClickListener(
                ParkedOnlyOnClickListener.create {
                    if (!isAddingItem) {
                        addCustomProduct(query)
                    }
                }
            )

            if (isDriverDistracted) {
                rowBuilder.addText(carContext.getString(R.string.car_app_voice_only_hint))
            }

            listBuilder.addItem(rowBuilder.build())
            hasRows = true
        }

        if (products.isEmpty() && !hasRows) {
            val noItemsMessage = if (query.isBlank()) {
                carContext.getString(R.string.car_app_search_hint)
            } else if (queryAlreadyInList) {
                carContext.getString(R.string.car_app_item_already_in_list)
            } else {
                carContext.getString(R.string.car_app_no_products)
            }
            listBuilder.setNoItemsMessage(noItemsMessage)
        } else {
            // Group products by category for better visual organization
            val productsByCategory = products.groupBy { it.category?.name ?: "Other" }
            
            productsByCategory.forEach { (categoryName, categoryProducts) ->
                // Add category header with icon
                if (categoryName != "Other" && productsByCategory.size > 1) {
                    listBuilder.addItem(
                        Row.Builder()
                            .setTitle("📁 $categoryName")
                            .build()
                    )
                }
                
                // Add products in category
                categoryProducts.forEach { product ->
                    val rowBuilder = Row.Builder().apply {
                        setTitle(product.name)
                        iconLoader.getOrLoad(product.icon)?.let { carIcon ->
                            setImage(carIcon, Row.IMAGE_TYPE_SMALL)
                        }

                        setOnClickListener(
                            ParkedOnlyOnClickListener.create {
                                if (!isAddingItem) {
                                    addExistingProduct(product)
                                }
                            }
                        )

                        if (isDriverDistracted) {
                            addText(carContext.getString(R.string.car_app_voice_only_hint))
                        }
                    }
                    listBuilder.addItem(rowBuilder.build())
                }
            }
        }

        return SearchTemplate.Builder(searchCallback)
            .setHeaderAction(Action.BACK)
            .setSearchHint(carContext.getString(R.string.car_app_search_hint))
            .setShowKeyboardByDefault(!isDriverDistracted)
            .setItemList(listBuilder.build())
            .setLoading(false)
            .build()
    }

    private fun addExistingProduct(product: Product) {
        if (isDuplicateProduct(product, groceries)) {
            invalidate()
            return
        }

        isAddingItem = true
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
        if (isDuplicateGroceryName(normalizedName, groceries)) {
            invalidate()
            return
        }

        isAddingItem = true
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


    private companion object {
        const val MAX_PRODUCTS = 120
        val RESTRICTION_POLL_INTERVAL = 1.seconds
    }
}

internal fun availableProductsForCarApp(
    products: List<Product>,
    groceries: List<Grocery>
): List<Product> {
    val groceryProductIds = groceries.asSequence().map { it.productId }.toSet()
    val groceryNameKeys = groceries.asSequence().map { normalizeGroceryName(it.name) }.toSet()

    return products
        .asSequence()
        .distinctBy { normalizeGroceryName(it.name) }
        .filterNot { product ->
            product.id in groceryProductIds || normalizeGroceryName(product.name) in groceryNameKeys
        }
        .toList()
}

internal fun isDuplicateProduct(
    product: Product,
    groceries: List<Grocery>
): Boolean = availableProductsForCarApp(listOf(product), groceries).isEmpty()

internal fun isDuplicateGroceryName(
    name: String,
    groceries: List<Grocery>
): Boolean {
    val normalizedName = normalizeGroceryName(name)
    return normalizedName.isNotEmpty() && groceries.any {
        normalizeGroceryName(it.name) == normalizedName
    }
}

internal fun normalizeGroceryName(name: String): String =
    name.trim().replace(WHITESPACE_REGEX, " ").lowercase()

private val WHITESPACE_REGEX = Regex("\\s+")

