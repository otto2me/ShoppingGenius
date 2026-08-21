package com.rendox.shoppinggenius.feature.editgrocery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.shoppinggenius.data.category.CategoryRepository
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.CompoundGroceryId
import com.rendox.shoppinggenius.model.Grocery
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class EditGroceryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val compoundGroceryIdFlow = MutableStateFlow<CompoundGroceryId?>(null)

    private val _uiStateFlow = MutableStateFlow(EditGroceryUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    var editGroceryDescription by mutableStateOf(TextFieldValue(""))
        private set
    private val editGroceryDescriptionFlow = snapshotFlow { editGroceryDescription.text }

    private var updateGroceryJob: Job? = null

    init {
        viewModelScope.launch {
            editGroceryDescriptionFlow.collectLatest { description ->
                _uiStateFlow.update {
                    it.copy(
                        clearEditGroceryDescriptionButtonIsShown = description.isNotEmpty()
                    )
                }
            }
        }
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .map { categories -> categories.sortedBy { it.sortingPriority } }
                .collectLatest { categories ->
                    _uiStateFlow.update {
                        it.copy(groceryCategories = categories)
                    }
                }
        }
    }

    fun onIntent(intent: EditGroceryUiIntent) = when (intent) {
        is EditGroceryUiIntent.OnDescriptionChanged ->
            editGroceryDescription = intent.description

        is EditGroceryUiIntent.OnClearDescription ->
            editGroceryDescription = TextFieldValue("")

        is EditGroceryUiIntent.OnCategorySelected ->
            onCategorySelected(intent.category)

        is EditGroceryUiIntent.OnCustomCategorySelected ->
            onCategorySelected(null)

        is EditGroceryUiIntent.OnCreateCategory ->
            onCreateCategory(intent.name)

        is EditGroceryUiIntent.OnToggleFavorite ->
            onToggleFavorite()

        is EditGroceryUiIntent.OnRemoveGroceryFromList ->
            onRemoveGroceryFromList()

        is EditGroceryUiIntent.OnDeleteProduct ->
            onDeleteProduct()

        is EditGroceryUiIntent.OnEditProduct ->
            onEditProduct(intent.productId)

        is EditGroceryUiIntent.OnEditOtherGrocery ->
            onEditOtherGrocery(intent.productId, intent.groceryListId)
    }

    private fun onCategorySelected(category: Category?) {
        viewModelScope.launch {
            val compoundGroceryId = compoundGroceryIdFlow.value
            if (compoundGroceryId != null) {
                val grocery = groceryRepository.getGroceryById(
                    productId = compoundGroceryId.productId,
                    listId = compoundGroceryId.groceryListId
                ).first() ?: return@launch

                if (grocery.productIsDefault) {
                    // Default products should not be changed so we create a new custom one
                    val newProductId = UUID.randomUUID().toString()
                    groceryRepository.insertProductAndGrocery(
                        name = grocery.name,
                        iconId = grocery.icon?.uniqueFileName,
                        productId = newProductId,
                        categoryId = category?.id,
                        groceryListId = compoundGroceryId.groceryListId,
                        description = grocery.description,
                        purchased = grocery.purchased,
                        purchasedLastModified = grocery.purchasedLastModified,
                        isDefault = false
                    )
                    productRepository.updateProductFavorite(
                        productId = newProductId,
                        isFavorite = grocery.isFavorite
                    )
                    groceryRepository.removeGroceryFromList(
                        productId = compoundGroceryId.productId,
                        listId = compoundGroceryId.groceryListId
                    )
                    onEditOtherGrocery(
                        productId = newProductId,
                        groceryListId = compoundGroceryId.groceryListId
                    )
                } else {
                    productRepository.updateProductCategory(
                        productId = compoundGroceryId.productId,
                        categoryId = category?.id
                    )
                }
            } else {
                val productId = _uiStateFlow.value.editGrocery?.productId ?: return@launch
                productRepository.updateProductCategory(
                    productId = productId,
                    categoryId = category?.id
                )
            }
            _uiStateFlow.update { uiState ->
                uiState.copy(
                    editGrocery = uiState.editGrocery?.copy(category = category)
                )
            }
        }
    }

    private fun onCreateCategory(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val category = categoryRepository.createCategory(trimmedName)
            onCategorySelected(category)
        }
    }

    private fun onRemoveGroceryFromList() {
        viewModelScope.launch {
            compoundGroceryIdFlow.value?.let { (productId, groceryListId) ->
                groceryRepository.removeGroceryFromList(
                    productId = productId,
                    listId = groceryListId
                )
            }
        }
    }

    private fun onToggleFavorite() {
        viewModelScope.launch {
            val currentGrocery = _uiStateFlow.value.editGrocery ?: return@launch
            val newFavoriteState = !currentGrocery.isFavorite
            productRepository.updateProductFavorite(
                productId = currentGrocery.productId,
                isFavorite = newFavoriteState
            )
            _uiStateFlow.update { uiState ->
                uiState.copy(editGrocery = uiState.editGrocery?.copy(isFavorite = newFavoriteState))
            }
        }
    }

    private fun onDeleteProduct() {
        viewModelScope.launch {
            val productId =
                compoundGroceryIdFlow.value?.productId ?: _uiStateFlow.value.editGrocery?.productId
            productId?.let { productRepository.deleteProductById(it) }
        }
    }

    private fun onEditProduct(productId: String) {
        compoundGroceryIdFlow.value = null
        updateGroceryJob?.cancel()
        updateGroceryJob = null
        viewModelScope.launch {
            val product = productRepository.getProductById(productId).first() ?: return@launch
            editGroceryDescription = TextFieldValue("")
            _uiStateFlow.update {
                it.copy(
                    editGrocery = Grocery(
                        productId = product.id,
                        name = product.name,
                        purchased = false,
                        description = null,
                        icon = product.icon,
                        category = product.category,
                        productIsDefault = product.isDefault,
                        isFavorite = product.isFavorite
                    ),
                    clearEditGroceryDescriptionButtonIsShown = false,
                    showRemoveFromListButton = false
                )
            }
        }
    }

    private fun onEditOtherGrocery(
        productId: String,
        groceryListId: String
    ) {
        compoundGroceryIdFlow.update {
            CompoundGroceryId(
                productId = productId,
                groceryListId = groceryListId
            )
        }
        updateGroceryJob?.cancel()
        updateGroceryJob = viewModelScope.launch {
            val grocery = groceryRepository.getGroceryById(
                productId = productId,
                listId = groceryListId
            ).first() ?: return@launch
            val nameLength = grocery.description?.length ?: 0
            editGroceryDescription = TextFieldValue(
                text = grocery.description ?: "",
                selection = TextRange(nameLength, nameLength)
            )
            _uiStateFlow.update {
                it.copy(
                    editGrocery = grocery,
                    showRemoveFromListButton = true
                )
            }
            editGroceryDescriptionFlow
                .debounce(800)
                .collectLatest { description ->
                    compoundGroceryIdFlow.value?.let { (productId, groceryListId) ->
                        groceryRepository.updateDescription(
                            productId = productId,
                            listId = groceryListId,
                            description = description.ifEmpty { null }
                        )
                    }
                }
        }
    }
}
