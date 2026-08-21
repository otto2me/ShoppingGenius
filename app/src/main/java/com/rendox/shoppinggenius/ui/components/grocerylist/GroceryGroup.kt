package com.rendox.shoppinggenius.ui.components.grocerylist

import androidx.annotation.StringRes
import com.rendox.shoppinggenius.model.Grocery

data class GroceryGroup(
    @StringRes val titleId: Int?,
    val groceries: List<Grocery>
)
