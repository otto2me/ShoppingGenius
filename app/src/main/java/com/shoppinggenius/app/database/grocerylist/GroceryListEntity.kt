package com.shoppinggenius.app.database.grocerylist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shoppinggenius.app.model.GroceryListType

@Entity
data class GroceryListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortingPriority: Long,
    val type: GroceryListType = GroceryListType.SHOPPING
)
