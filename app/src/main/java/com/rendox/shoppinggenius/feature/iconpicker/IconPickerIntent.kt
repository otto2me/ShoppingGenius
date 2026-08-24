package com.rendox.shoppinggenius.feature.iconpicker

import com.rendox.shoppinggenius.model.IconReference

sealed interface IconPickerIntent {
    data class OnPickIcon(val iconReference: IconReference) : IconPickerIntent
    data class OnPickRemoteImage(
        val imageUrl: String,
        val fallbackImageUrl: String? = null
    ) : IconPickerIntent
    /** Bestätigt das ausgewählte DDG-Bild und speichert es als Icon. */
    data object OnConfirmRemoteIcon : IconPickerIntent
    data class OnUpdateSearchQuery(val query: String) : IconPickerIntent
    data object OnClearSearchQuery : IconPickerIntent
    data object OnRemoveIcon : IconPickerIntent
    data class OnDeleteIcon(val iconReference: IconReference) : IconPickerIntent
}
