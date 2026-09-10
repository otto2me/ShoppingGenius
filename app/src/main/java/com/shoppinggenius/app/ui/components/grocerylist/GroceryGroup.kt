package com.shoppinggenius.app.ui.components.grocerylist

import androidx.annotation.StringRes
import com.shoppinggenius.app.model.Grocery

data class GroceryGroup(
    @StringRes val titleId: Int?,
    val title: String? = null,
    val groceries: List<Grocery>
)
