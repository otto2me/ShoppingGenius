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
import com.shoppinggenius.app.model.GroceryListType
import com.shoppinggenius.app.model.Product
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

    private val restrictionsHelper = CarUxRestrictionsHelper(carContext)
    private val directVoiceRecognizer = CarDirectVoiceRecognizer(carContext)
    private var drivingState = restrictionsHelper.getDrivingState()

    private val addItemAction: Action
        get() {
            val action = Action.Builder().apply {
                if (drivingState == CarUxRestrictionsHelper.DrivingState.RESTRICTED) {
                    setIcon(carContext.defaultCarIcon(R.drawable.ic_mic_24))
                } else {
                    setTitle(carContext.getString(R.string.car_app_add_item))
                }
            }

            action.setOnClickListener {
                if (drivingState == CarUxRestrictionsHelper.DrivingState.RESTRICTED) {
                    startVoiceInput()
                } else {
                    screenManager.push(
                        ProductPickerScreen(
                            carContext = carContext,
                            groceryList = groceryList,
                            groceryRepository = groceryRepository,
                            productRepository = productRepository,
                            preferVoiceInput = false
                        )
                    )
                }
            }

            return action.build()
        }

    private var groceries: List<Grocery> = emptyList()
    private var allProducts: List<Product> = emptyList()
    private var isLoading = true
    private var isVoiceInputInProgress = false
    private var lastVoiceStatusMessage: String? = null

    private fun showVoiceFeedback(message: String) {
        lastVoiceStatusMessage = message
        carContext.showCarToast(message, androidx.car.app.CarToast.LENGTH_LONG)
        invalidate()
    }

    init {
        lifecycleScope.launch {
            if (groceryList.type == GroceryListType.SHOPPING) {
                productRepository.getAllProducts().collectLatest { products ->
                    allProducts = products.sortedBy { it.name.lowercase() }
                }
            }
        }
        lifecycleScope.launch {
            groceryRepository.getGroceriesFromList(groceryList.id).collectLatest { items ->
                groceries = items
                isLoading = false
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

    private fun currentVoicePrompt(): String = carContext.getString(
        if (groceryList.type == GroceryListType.TODO) {
            R.string.add_entry_voice_input_prompt
        } else {
            R.string.add_grocery_voice_input_prompt
        }
    )

    private fun startVoiceInput() {
        if (isVoiceInputInProgress) {
            showVoiceFeedback(carContext.getString(R.string.car_app_voice_input_listening))
            return
        }

        showVoiceFeedback(carContext.getString(R.string.car_app_voice_input_starting))

        when (
            directVoiceRecognizer.start(
                prompt = currentVoicePrompt(),
                onReady = {
                    showVoiceFeedback(currentVoicePrompt())
                },
                onListening = {
                    showVoiceFeedback(carContext.getString(R.string.car_app_voice_input_listening))
                },
                onResult = { spokenText ->
                    isVoiceInputInProgress = false
                    addSpokenItem(spokenText)
                },
                onCancelled = {
                    isVoiceInputInProgress = false
                    invalidate()
                }
            )
        ) {
            CarDirectVoiceRecognizer.StartResult.STARTED,
            CarDirectVoiceRecognizer.StartResult.ALREADY_RUNNING -> {
                isVoiceInputInProgress = true
            }

            CarDirectVoiceRecognizer.StartResult.PERMISSION_REQUIRED,
            CarDirectVoiceRecognizer.StartResult.UNAVAILABLE,
            CarDirectVoiceRecognizer.StartResult.FAILED -> {
                isVoiceInputInProgress = false
                showVoiceFeedback(carContext.getString(R.string.add_grocery_voice_input_not_available))
            }
        }
    }

    private fun addSpokenItem(spokenText: String) {
        val normalizedName = spokenText.trim()
        if (normalizedName.isBlank()) {
            invalidate()
            return
        }

        if (isDuplicateGroceryName(normalizedName, groceries)) {
            carContext.showDrivingRestrictedToast(
                carContext.getString(R.string.car_app_item_already_in_list)
            )
            invalidate()
            return
        }

        if (groceryList.type == GroceryListType.SHOPPING) {
            val exactProduct = availableProductsForCarApp(allProducts, groceries).firstOrNull {
                normalizeGroceryName(it.name) == normalizeGroceryName(normalizedName)
            }

            if (exactProduct != null) {
                addExistingProductFromVoice(exactProduct)
                return
            }
        }

        addCustomProductFromVoice(normalizedName)
    }

    private fun addExistingProductFromVoice(product: Product) {
        lifecycleScope.launch {
            groceryRepository.addGroceryToList(
                productId = product.id,
                listId = groceryList.id,
                purchased = false
            )
            carContext.showCarToast(carContext.getString(R.string.car_app_item_added, product.name))
        }
    }

    private fun addCustomProductFromVoice(name: String) {
        lifecycleScope.launch {
            groceryRepository.insertProductAndGrocery(
                name = name,
                categoryId = null,
                groceryListId = groceryList.id,
                description = null,
                purchased = false
            )
            carContext.showCarToast(carContext.getString(R.string.car_app_item_added, name))
        }
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

        if (isDriverDistracted) {
            rowBuilder.setOnClickListener(
                {
                    carContext.showDrivingRestrictedToast()
                }
            )
        } else {
            rowBuilder.setToggle(
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
        }

        rowBuilder.setImage(
            carContext.defaultCarIcon(
                if (grocery.purchased) R.drawable.checkmark_circle else R.drawable.shopping_cart
            ),
            Row.IMAGE_TYPE_ICON
        )

        val subtitle = grocery.category?.name
            ?: grocery.description?.takeIf { it.isNotBlank() }
        subtitle?.let { rowBuilder.addText(it) }

        listBuilder.addItem(rowBuilder.build())
    }

    private companion object {
        val RESTRICTION_POLL_INTERVAL = 1.seconds
    }
}

