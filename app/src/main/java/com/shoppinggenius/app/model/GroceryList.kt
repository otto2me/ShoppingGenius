package com.shoppinggenius.app.model

import java.util.UUID

data class GroceryList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sortingPriority: Long = System.currentTimeMillis(),
    val type: GroceryListType = GroceryListType.SHOPPING,
    val numOfGroceries: Int = 0
)
