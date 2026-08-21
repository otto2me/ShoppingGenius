package com.rendox.shoppinggenius.data.model

import com.rendox.shoppinggenius.database.grocerylist.GroceryListEntity
import com.rendox.shoppinggenius.model.GroceryList

fun GroceryList.asEntity() = GroceryListEntity(
    id = id,
    name = name,
    sortingPriority = sortingPriority
)
