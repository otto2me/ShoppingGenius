package com.rendox.shoppinggenius.data.model

import com.rendox.shoppinggenius.database.grocery.CombinedGrocery
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.IconReference

fun CombinedGrocery.asExternalModel() = Grocery(
    productId = productId,
    name = name,
    purchased = purchased,
    description = description,
    icon = icon,
    category = category,
    purchasedLastModified = purchasedLastModified,
    productIsDefault = productIsDefault,
    isFavorite = productIsFavorite
)

val CombinedGrocery.icon
    get() = when {
        iconId != null && iconFilePath != null -> IconReference(
            uniqueFileName = iconId,
            filePath = iconFilePath,
            name = this.name
        )

        else -> null
    }

val CombinedGrocery.category
    get() = when {
        categoryId != null &&
            categoryName != null &&
            categorySortingPriority != null -> Category(
            id = categoryId,
            name = categoryName,
            sortingPriority = categorySortingPriority
        )

        else -> null
    }
