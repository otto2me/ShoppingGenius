package com.shoppinggenius.app.database.product

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shoppinggenius.app.database.category.CategoryEntity
import com.shoppinggenius.app.database.groceryicon.IconEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = IconEntity::class,
            parentColumns = ["uniqueFileName"],
            childColumns = ["iconFileName"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["iconFileName"]),
        Index(value = ["categoryId"]),
        Index(value = ["ownerGroceryListId"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDefault: Boolean,
    val isFavorite: Boolean,
    val iconFileName: String?,
    val categoryId: String?,
    val showInCatalog: Boolean = true,
    val ownerGroceryListId: String? = null
)
