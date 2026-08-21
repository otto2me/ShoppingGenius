package com.rendox.shoppinggenius.feature.iconpicker

import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.model.Product

data class IconPickerUiState(
    val groupedIcons: Map<Category, List<IconReference>> = emptyMap(),
    val searchResults: List<IconReference> = emptyList(),
    val duckDuckGoImageResults: List<DuckDuckGoImageSearchResult> = emptyList(),
    val product: Product? = null,
    val previewIcon: IconReference? = null,
    val clearSearchQueryButtonIsShown: Boolean = false,
    val searchResultsShown: Boolean = false,
    val remoteImportInProgress: Boolean = false,
    val importingImageUrl: String? = null,
    val remoteImportSucceeded: Boolean? = null,
    val remoteImportEventId: Long = 0L
)
