package com.shoppinggenius.app.data.model

import com.shoppinggenius.app.database.grocery.CombinedGrocery
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.Grocery
import com.shoppinggenius.app.model.IconReference

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
