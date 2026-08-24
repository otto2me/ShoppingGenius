package com.rendox.shoppinggenius.ui.components.grocerylist

import androidx.annotation.StringRes
import com.rendox.shoppinggenius.model.Grocery

data class GroceryGroup(
    @StringRes val titleId: Int?,
    val title: String? = null,
    val groceries: List<Grocery>
)
