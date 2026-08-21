package com.rendox.shoppinggenius.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = BACKUP_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val groceryLists: List<BackupGroceryList> = emptyList(),
    val groceries: List<BackupGrocery> = emptyList(),
    val products: List<BackupProduct> = emptyList(),
    val customIcons: List<BackupIcon> = emptyList()
) {
    companion object {
        const val BACKUP_VERSION = 1
        const val MANIFEST_FILE = "backup.json"
        const val ICONS_DIR = "icons"
    }
}

@JsonClass(generateAdapter = true)
data class BackupGroceryList(
    val id: String,
    val name: String,
    val sortingPriority: Long
)

@JsonClass(generateAdapter = true)
data class BackupGrocery(
    val productId: String,
    val groceryListId: String,
    val description: String?,
    val purchased: Boolean,
    val purchasedLastModified: Long
)

@JsonClass(generateAdapter = true)
data class BackupProduct(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val isFavorite: Boolean,
    val iconFileName: String?,
    val categoryId: String?
)

@JsonClass(generateAdapter = true)
data class BackupIcon(
    val uniqueFileName: String,
    val filePath: String
)


