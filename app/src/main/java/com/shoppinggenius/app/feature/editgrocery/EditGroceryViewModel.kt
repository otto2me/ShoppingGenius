package com.shoppinggenius.app.feature.editgrocery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppinggenius.app.data.category.CategoryRepository
import com.shoppinggenius.app.data.grocery.GroceryRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.CompoundGroceryId
import com.shoppinggenius.app.model.Grocery
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

    var editGroceryName by mutableStateOf(TextFieldValue(""))
        private set
    private val editGroceryNameFlow = snapshotFlow { editGroceryName.text }

    var editGroceryDescription by mutableStateOf(TextFieldValue(""))
        private set
    private val editGroceryDescriptionFlow = snapshotFlow { editGroceryDescription.text }

    private var updateGroceryNameJob: Job? = null
    private var updateGroceryDescriptionJob: Job? = null
    private var persistedEditGroceryName: String? = null
    private var persistedEditGroceryDescription: String? = null

    init {
        viewModelScope.launch {
            editGroceryNameFlow.collectLatest { name ->
                _uiStateFlow.update {
                    it.copy(clearEditGroceryNameButtonIsShown = name.isNotEmpty())
                }
            }
        }
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
        is EditGroceryUiIntent.OnNameChanged ->
            editGroceryName = intent.name

        is EditGroceryUiIntent.OnClearName ->
            editGroceryName = TextFieldValue("")

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
                    val editableGrocery = materializeEditableCustomGrocery(
                        grocery = grocery,
                        groceryListId = compoundGroceryId.groceryListId,
                        category = category
                    )
                    persistedEditGroceryName = editableGrocery.name
                    persistedEditGroceryDescription = editableGrocery.description.orEmpty()
                    _uiStateFlow.update { uiState ->
                        uiState.copy(editGrocery = editableGrocery)
                    }
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
        updateGroceryNameJob?.cancel()
        updateGroceryDescriptionJob?.cancel()
        updateGroceryDescriptionJob = null
        viewModelScope.launch {
            val product = productRepository.getProductById(productId).first() ?: return@launch
            val nameLength = product.name.length
            editGroceryName = TextFieldValue(
                text = product.name,
                selection = TextRange(nameLength, nameLength)
            )
            editGroceryDescription = TextFieldValue("")
            persistedEditGroceryName = product.name
            persistedEditGroceryDescription = ""
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
                    clearEditGroceryNameButtonIsShown = product.name.isNotEmpty(),
                    clearEditGroceryDescriptionButtonIsShown = false,
                    nameCanBeModified = !product.isDefault,
                    showRemoveFromListButton = false
                )
            }
            startNameUpdates()
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
        updateGroceryNameJob?.cancel()
        updateGroceryDescriptionJob?.cancel()
        viewModelScope.launch {
            val grocery = groceryRepository.getGroceryById(
                productId = productId,
                listId = groceryListId
            ).first() ?: return@launch
            val nameLength = grocery.name.length
            editGroceryName = TextFieldValue(
                text = grocery.name,
                selection = TextRange(nameLength, nameLength)
            )
            val descriptionLength = grocery.description?.length ?: 0
            editGroceryDescription = TextFieldValue(
                text = grocery.description ?: "",
                selection = TextRange(descriptionLength, descriptionLength)
            )
            persistedEditGroceryName = grocery.name
            persistedEditGroceryDescription = grocery.description.orEmpty()
            _uiStateFlow.update {
                it.copy(
                    editGrocery = grocery,
                    clearEditGroceryNameButtonIsShown = grocery.name.isNotEmpty(),
                    nameCanBeModified = true,
                    showRemoveFromListButton = true
                )
            }
            startNameUpdates()
            startDescriptionUpdates()
        }
    }

    private fun startNameUpdates() {
        updateGroceryNameJob?.cancel()
        updateGroceryNameJob = viewModelScope.launch {
            editGroceryNameFlow
                .debounce(800)
                .collectLatest { name ->
                    persistNameIfChanged(name)
                }
        }
    }

    private fun startDescriptionUpdates() {
        updateGroceryDescriptionJob?.cancel()
        updateGroceryDescriptionJob = viewModelScope.launch {
            editGroceryDescriptionFlow
                .debounce(800)
                .collectLatest { description ->
                    persistDescriptionIfChanged(description)
                }
        }
    }

    private suspend fun persistNameIfChanged(rawName: String) {
        val trimmedName = rawName.trim()
        val currentGrocery = _uiStateFlow.value.editGrocery ?: return
        if (!_uiStateFlow.value.nameCanBeModified || trimmedName.isEmpty() || trimmedName == persistedEditGroceryName) {
            return
        }

        val updatedGrocery = compoundGroceryIdFlow.value?.let { compoundGroceryId ->
            if (currentGrocery.productIsDefault) {
                materializeEditableCustomGrocery(
                    grocery = currentGrocery,
                    groceryListId = compoundGroceryId.groceryListId,
                    name = trimmedName
                )
            } else {
                productRepository.updateProductName(currentGrocery.productId, trimmedName)
                currentGrocery.copy(name = trimmedName)
            }
        } ?: run {
            if (currentGrocery.productIsDefault) return
            productRepository.updateProductName(currentGrocery.productId, trimmedName)
            currentGrocery.copy(name = trimmedName)
        }

        persistedEditGroceryName = updatedGrocery.name
        _uiStateFlow.update { uiState ->
            uiState.copy(editGrocery = updatedGrocery)
        }
    }

    private suspend fun persistDescriptionIfChanged(description: String) {
        if (description == persistedEditGroceryDescription) return
        compoundGroceryIdFlow.value?.let { (productId, groceryListId) ->
            groceryRepository.updateDescription(
                productId = productId,
                listId = groceryListId,
                description = description.ifEmpty { null }
            )
            persistedEditGroceryDescription = description
            _uiStateFlow.update { uiState ->
                uiState.copy(
                    editGrocery = uiState.editGrocery?.copy(description = description.ifEmpty { null })
                )
            }
        }
    }

    private suspend fun materializeEditableCustomGrocery(
        grocery: Grocery,
        groceryListId: String,
        name: String = editGroceryName.text.trim().ifEmpty { grocery.name },
        category: Category? = grocery.category,
        description: String? = editGroceryDescription.text.ifEmpty { null }
    ): Grocery {
        val newProductId = UUID.randomUUID().toString()
        groceryRepository.insertProductAndGrocery(
            name = name,
            iconId = grocery.icon?.uniqueFileName,
            productId = newProductId,
            categoryId = category?.id,
            groceryListId = groceryListId,
            description = description,
            purchased = grocery.purchased,
            purchasedLastModified = grocery.purchasedLastModified,
            isDefault = false
        )
        productRepository.updateProductFavorite(
            productId = newProductId,
            isFavorite = grocery.isFavorite
        )
        groceryRepository.removeGroceryFromList(
            productId = grocery.productId,
            listId = groceryListId
        )
        compoundGroceryIdFlow.value = CompoundGroceryId(
            productId = newProductId,
            groceryListId = groceryListId
        )
        return grocery.copy(
            productId = newProductId,
            name = name,
            description = description,
            category = category,
            productIsDefault = false
        )
    }
}
