package com.shoppinggenius.app.database.grocery

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.shoppinggenius.app.database.grocerylist.GroceryListEntity
import com.shoppinggenius.app.database.product.ProductEntity

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
