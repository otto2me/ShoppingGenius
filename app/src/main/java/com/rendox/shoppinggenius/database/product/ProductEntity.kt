package com.rendox.shoppinggenius.database.product

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rendox.shoppinggenius.database.category.CategoryEntity
import com.rendox.shoppinggenius.database.groceryicon.IconEntity

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
        Index(value = ["categoryId"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDefault: Boolean,
    val isFavorite: Boolean,
    val iconFileName: String?,
    val categoryId: String?
)
