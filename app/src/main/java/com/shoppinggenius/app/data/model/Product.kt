package com.shoppinggenius.app.data.model

import com.shoppinggenius.app.database.product.CombinedProduct
import com.shoppinggenius.app.database.product.ProductEntity
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.IconReference
import com.shoppinggenius.app.model.Product
import com.shoppinggenius.app.network.model.ProductNetwork

fun Product.asEntity() = ProductEntity(
    id = id,
    name = name,
    categoryId = category?.id,
    iconFileName = icon?.uniqueFileName,
    isDefault = isDefault,
    isFavorite = isFavorite
)

fun CombinedProduct.asExternalModel() = Product(
    id = id,
    name = name,
    icon = icon,
    category = category,
    isDefault = isDefault,
    isFavorite = isFavorite
)

val CombinedProduct.icon
    get() = when {
        iconId != null && iconFilePath != null -> IconReference(
            uniqueFileName = iconId,
            filePath = iconFilePath,
            name = this.name
        )

        else -> null
    }

val CombinedProduct.category
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

fun ProductNetwork.asEntity() = ProductEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    iconFileName = iconId,
    isDefault = isDefault,
    isFavorite = false
)
