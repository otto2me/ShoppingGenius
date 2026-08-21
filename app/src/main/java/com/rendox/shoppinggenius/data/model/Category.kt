package com.rendox.shoppinggenius.data.model

import com.rendox.shoppinggenius.database.category.CategoryEntity
import com.rendox.shoppinggenius.database.category.CombinedCategory
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.network.model.CategoryNetwork

fun CombinedCategory.asExternalModel() = Category(
    id = id,
    name = name,
    sortingPriority = sortingPriority,
    defaultSortingPriority = defaultSortingPriority,
    icon = iconId?.let { uniqueFileName ->
        IconReference(
            uniqueFileName = uniqueFileName,
            filePath = iconFilePath ?: "",
            name = name
        )
    }
)

fun CategoryEntity.asExternalModel() = Category(
    id = id,
    name = name,
    sortingPriority = sortingPriority,
    defaultSortingPriority = defaultSortingPriority,
    icon = iconFileName?.let { uniqueFileName ->
        IconReference(
            uniqueFileName = uniqueFileName,
            filePath = "",
            name = name
        )
    }
)

fun Category.asEntity() = CategoryEntity(
    id = id,
    name = name,
    sortingPriority = sortingPriority,
    defaultSortingPriority = defaultSortingPriority,
    iconFileName = icon?.uniqueFileName
)

fun CategoryNetwork.asEntity(iconFileName: String? = null) = CategoryEntity(
    id = id,
    name = name,
    sortingPriority = sortingPriority,
    defaultSortingPriority = sortingPriority,
    iconFileName = iconFileName
)
