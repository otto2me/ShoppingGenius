package com.rendox.shoppinggenius.feature.iconpicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.shoppinggenius.data.category.CategoryRepository
import com.rendox.shoppinggenius.data.icons.IconRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryIconPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val iconRepository: IconRepository,
    private val categoryRepository: CategoryRepository,
    private val duckDuckGoImageSearchService: DuckDuckGoImageSearchService
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle[CATEGORY_ID_ARG])

    var searchQuery by mutableStateOf("")
        private set
    private val searchQueryFlow = snapshotFlow { searchQuery }

    private val _uiStateFlow = MutableStateFlow(IconPickerUiState())
    val uiStateFlow: StateFlow<IconPickerUiState> = _uiStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            searchQueryFlow.collectLatest { query ->
                val trimmed = query.trim()
                _uiStateFlow.update {
                    it.copy(
                        clearSearchQueryButtonIsShown = query.isNotEmpty(),
                        searchResultsShown = trimmed.isNotEmpty()
                    )
                }
                val results = iconRepository.getGroceryIconsByName("%$trimmed%")
                    .sortedWith(
                        compareBy(
                            { it.name?.startsWith(query, ignoreCase = true) == false },
                            { it.name ?: "" }
                        )
                    )
                _uiStateFlow.update { it.copy(searchResults = results) }

                val duckDuckGoResults = duckDuckGoImageSearchService.searchImages(trimmed)
                _uiStateFlow.update { it.copy(duckDuckGoImageResults = duckDuckGoResults) }
            }
        }
        viewModelScope.launch {
            iconRepository.getIconsGroupedByCategory().collectLatest { groupedIcons ->
                _uiStateFlow.update {
                    it.copy(
                        groupedIcons = groupedIcons.toSortedMap(
                            compareBy { category -> category.sortingPriority }
                        )
                    )
                }
            }
        }
    }

    fun onIntent(intent: IconPickerIntent) = viewModelScope.launch {
        when (intent) {
            is IconPickerIntent.OnPickIcon -> {
                _uiStateFlow.update { it.copy(pendingRemoteIconRef = null) }
                categoryRepository.updateCategoryIcon(categoryId, intent.iconReference.uniqueFileName)
            }
            is IconPickerIntent.OnConfirmRemoteIcon -> {
                val pending = _uiStateFlow.value.pendingRemoteIconRef ?: return@launch
                _uiStateFlow.update { it.copy(pendingRemoteIconRef = null) }
                categoryRepository.updateCategoryIcon(categoryId, pending.uniqueFileName)
            }
            is IconPickerIntent.OnRemoveIcon -> {
                _uiStateFlow.update { it.copy(pendingRemoteIconRef = null) }
                categoryRepository.updateCategoryIcon(categoryId, null)
            }
            is IconPickerIntent.OnUpdateSearchQuery ->
                searchQuery = intent.query
            is IconPickerIntent.OnClearSearchQuery ->
                searchQuery = ""
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
                    // categoryRepository.updateCategoryIcon() wird NICHT hier aufgerufen
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
}
