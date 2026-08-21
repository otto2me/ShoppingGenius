package com.rendox.shoppinggenius.data.model

import com.rendox.shoppinggenius.database.product.CombinedProduct
import com.rendox.shoppinggenius.database.product.ProductEntity
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.model.Product
import com.rendox.shoppinggenius.network.model.ProductNetwork

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
