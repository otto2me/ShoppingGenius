package com.rendox.grocerygenius.data.model

import com.rendox.grocerygenius.database.category.CategoryEntity
import com.rendox.grocerygenius.database.category.CombinedCategory
import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.model.IconReference
import com.rendox.grocerygenius.network.model.CategoryNetwork

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