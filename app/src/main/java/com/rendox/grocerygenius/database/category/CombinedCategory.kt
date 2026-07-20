package com.rendox.grocerygenius.database.category

data class CombinedCategory(
    val id: String,
    val name: String,
    val sortingPriority: Long,
    val defaultSortingPriority: Long,
    val iconId: String?,
    val iconFilePath: String?
)