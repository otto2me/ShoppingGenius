package com.rendox.grocerygenius.database.grocery

data class WidgetGroceryItem(
    val productId: String,
    val name: String,
    val description: String?,
    val purchased: Boolean,
    val iconFilePath: String?
)

