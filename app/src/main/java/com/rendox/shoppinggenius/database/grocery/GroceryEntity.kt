package com.rendox.shoppinggenius.database.grocery

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.rendox.shoppinggenius.database.grocerylist.GroceryListEntity
import com.rendox.shoppinggenius.database.product.ProductEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroceryListEntity::class,
            parentColumns = ["id"],
            childColumns = ["groceryListId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["productId", "groceryListId"],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["groceryListId"])
    ]
)
data class GroceryEntity(
    val productId: String,
    val groceryListId: String,
    val description: String?,
    val purchased: Boolean,
    val purchasedLastModified: Long
)
