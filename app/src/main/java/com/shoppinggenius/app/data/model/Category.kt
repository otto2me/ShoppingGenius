package com.shoppinggenius.app.data.model

import com.shoppinggenius.app.database.category.CategoryEntity
import com.shoppinggenius.app.database.category.CombinedCategory
import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.IconReference
import com.shoppinggenius.app.network.model.CategoryNetwork

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
