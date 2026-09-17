package com.shoppinggenius.app.data.model

import com.shoppinggenius.app.database.grocerylist.GroceryListEntity
import com.shoppinggenius.app.model.GroceryList

fun GroceryList.asEntity() = GroceryListEntity(
    id = id,
    name = name,
    sortingPriority = sortingPriority,
    type = type
)
