package com.rendox.shoppinggenius.feature.iconpicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.icons.IconRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IconPickerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val iconRepository: IconRepository,
    private val productRepository: ProductRepository,
    private val groceryRepository: GroceryRepository,
    private val duckDuckGoImageSearchService: DuckDuckGoImageSearchService
) : ViewModel() {

    private val editProductIdFlow: StateFlow<String?> = savedStateHandle.getStateFlow(
        key = PRODUCT_ID_ARG,
        initialValue = null
    )
    private val groceryListId: String =
        checkNotNull(savedStateHandle[ICON_PICKER_GROCERY_LIST_ID_ARG])

    var searchQuery by mutableStateOf("")
        private set
    private var hasInitializedSearchQuery = false
    private val searchQueryFlow = snapshotFlow { searchQuery }

    private val _uiStateFlow = MutableStateFlow(IconPickerUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            searchQueryFlow.collectLatest { query ->
                val trimmedQuery = query.trim()
                _uiStateFlow.update { uiState ->
                    uiState.copy(
                        clearSearchQueryButtonIsShown = query.isNotEmpty(),
                        searchResultsShown = trimmedQuery.isNotEmpty()
                    )
                }
                val searchResults = iconRepository.getGroceryIconsByName("%$trimmedQuery%")
                    .sortedWith(
                        compareBy(
                            { it.name?.startsWith(searchQuery, ignoreCase = true) == false },
                            { it.name ?: "" }
                        )
                    )
                _uiStateFlow.update { it.copy(searchResults = searchResults) }

                val duckDuckGoResults = duckDuckGoImageSearchService.searchImages(trimmedQuery)
                _uiStateFlow.update { it.copy(duckDuckGoImageResults = duckDuckGoResults) }
            }
        }
        viewModelScope.launch {
            iconRepository.getIconsGroupedByCategory().collectLatest { groupedIcons ->
                _uiStateFlow.update { uiState ->
                    uiState.copy(
                        groupedIcons = groupedIcons.toSortedMap(
                            comparator = compareBy { category -> category.sortingPriority }
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            editProductIdFlow
                .mapNotNull { it }
                .flatMapLatest { productId ->
                    productRepository.getProductById(productId)
                }
                .collectLatest { product ->
                    if (!hasInitializedSearchQuery) {
                        val initialSearchQuery = product?.name?.trim().orEmpty()
                        if (initialSearchQuery.isNotEmpty()) {
                            searchQuery = initialSearchQuery
                        }
                        hasInitializedSearchQuery = true
                    }
                    _uiStateFlow.update { state ->
                        state.copy(
                            product = product,
                            previewIcon = state.previewIcon?.takeUnless {
                                it.uniqueFileName == product?.icon?.uniqueFileName
                            }
                        )
                    }
                }
        }
    }

    fun onIntent(intent: IconPickerIntent) = viewModelScope.launch {
        when (intent) {
            is IconPickerIntent.OnPickIcon -> {
                // Lokales Icon direkt übernehmen; ggf. pendingRemoteIconRef verwerfen
                _uiStateFlow.update {
                    it.copy(
                        previewIcon = intent.iconReference,
                        pendingRemoteIconRef = null
                    )
                }
                onPickIcon(intent.iconReference.uniqueFileName)
            }

            is IconPickerIntent.OnConfirmRemoteIcon -> {
                val pending = _uiStateFlow.value.pendingRemoteIconRef ?: return@launch
                _uiStateFlow.update { it.copy(pendingRemoteIconRef = null) }
                onPickIcon(pending.uniqueFileName)
            }

            is IconPickerIntent.OnUpdateSearchQuery ->
                searchQuery = intent.query

            is IconPickerIntent.OnClearSearchQuery -> searchQuery = ""
            is IconPickerIntent.OnRemoveIcon -> {
                _uiStateFlow.update { it.copy(previewIcon = null, pendingRemoteIconRef = null) }
                onPickIcon(null)
            }
            is IconPickerIntent.OnDeleteIcon -> {
                iconRepository.deleteIcon(intent.iconReference.uniqueFileName)
            }
            is IconPickerIntent.OnPickRemoteImage -> {
                _uiStateFlow.update {
                    it.copy(
                        remoteImportInProgress = true,
                        importingImageUrl = intent.imageUrl
                    )
                }
                val iconRef = iconRepository.importCustomIconFromUrl(
                    imageUrl = intent.imageUrl,
                    fallbackImageUrl = intent.fallbackImageUrl
                )
                if (iconRef != null) {
                    _uiStateFlow.update {
                        it.copy(
                            previewIcon = iconRef,
                            pendingRemoteIconRef = iconRef,   // wartet auf Bestätigung
                            remoteImportInProgress = false,
                            importingImageUrl = null,
                            remoteImportSucceeded = true,
                            remoteImportEventId = it.remoteImportEventId + 1
                        )
                    }
                    // onPickIcon() wird NICHT hier aufgerufen – erst nach Bestätigung
                } else {
                    _uiStateFlow.update {
                        it.copy(
                            remoteImportInProgress = false,
                            importingImageUrl = null,
                            remoteImportSucceeded = false,
                            remoteImportEventId = it.remoteImportEventId + 1
                        )
                    }
                }
            }
        }
    }

    private suspend fun onPickIcon(iconId: String?) {
        val editProductId = editProductIdFlow.value ?: return

        // Called from Listen screen — no grocery list context, update product icon directly.
        if (groceryListId == LISTEN_NO_LIST_ID) {
            productRepository.updateProductIcon(productId = editProductId, iconId = iconId)
            return
        }

        val grocery = groceryRepository.getGroceryById(
            productId = editProductId,
            listId = groceryListId
        ).first() ?: return

        if (grocery.productIsDefault) {
            // Default products should not be changed so we create a new custom one
            val newProductId = UUID.randomUUID().toString()
            groceryRepository.insertProductAndGrocery(
                name = grocery.name,
                iconId = iconId,
                productId = newProductId,
                categoryId = grocery.category?.id,
                groceryListId = groceryListId,
                description = grocery.description,
                purchased = grocery.purchased,
                purchasedLastModified = grocery.purchasedLastModified,
                isDefault = false
            )
            groceryRepository.removeGroceryFromList(
                productId = editProductId,
                listId = groceryListId
            )
            savedStateHandle[PRODUCT_ID_ARG] = newProductId
        } else {
            productRepository.updateProductIcon(
                productId = editProductId,
                iconId = iconId
            )
        }
    }
}
