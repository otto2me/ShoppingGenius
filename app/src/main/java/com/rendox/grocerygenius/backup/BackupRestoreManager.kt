package com.rendox.grocerygenius.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.rendox.grocerygenius.database.AppDatabase
import com.rendox.grocerygenius.database.grocery.GroceryEntity
import com.rendox.grocerygenius.database.groceryicon.IconEntity
import com.rendox.grocerygenius.database.grocerylist.GroceryListEntity
import com.rendox.grocerygenius.database.product.ProductEntity
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val moshi: Moshi
) {
    private val adapter by lazy { moshi.adapter(BackupData::class.java) }

    suspend fun exportBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val groceryLists = database.groceryListDao().getAllGroceryListEntities()
            val groceries = database.groceryDao().getAllGroceryEntities()
            // Export non-default products + default products with favorites or modified icon
            val products = database.productDao().getProductEntitiesForBackup()
            val customIcons = database.iconDao().getCustomIconEntities()

            val backupData = BackupData(
                groceryLists = groceryLists.map {
                    BackupGroceryList(it.id, it.name, it.sortingPriority)
                },
                groceries = groceries.map {
                    BackupGrocery(
                        productId = it.productId,
                        groceryListId = it.groceryListId,
                        description = it.description,
                        purchased = it.purchased,
                        purchasedLastModified = it.purchasedLastModified
                    )
                },
                products = products.map {
                    BackupProduct(
                        id = it.id,
                        name = it.name,
                        isDefault = it.isDefault,
                        isFavorite = it.isFavorite,
                        iconFileName = it.iconFileName,
                        categoryId = it.categoryId
                    )
                },
                customIcons = customIcons.map { BackupIcon(it.uniqueFileName, it.filePath) }
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream.buffered()).use { zip ->
                    // Write manifest JSON
                    zip.putNextEntry(ZipEntry(BackupData.MANIFEST_FILE))
                    zip.write(adapter.toJson(backupData).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()

                    // Write custom icon files
                    for (icon in customIcons) {
                        val iconFile = File(context.filesDir, icon.filePath)
                        if (iconFile.exists()) {
                            zip.putNextEntry(
                                ZipEntry("${BackupData.ICONS_DIR}/${icon.uniqueFileName}")
                            )
                            iconFile.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("Cannot open output stream for URI"))

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupRestore", "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun importBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val iconsDir = File(context.filesDir, "icons").also { it.mkdirs() }
            var backupData: BackupData? = null
            val iconFiles = mutableMapOf<String, ByteArray>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream.buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == BackupData.MANIFEST_FILE -> {
                                backupData = adapter.fromJson(
                                    zip.readBytes().toString(Charsets.UTF_8)
                                )
                            }
                            entry.name.startsWith("${BackupData.ICONS_DIR}/") -> {
                                val fileName = entry.name.substringAfterLast('/')
                                if (fileName.isNotEmpty()) {
                                    iconFiles[fileName] = zip.readBytes()
                                }
                            }
                        }
                        entry = zip.nextEntry
                    }
                }
            } ?: return@withContext Result.failure(Exception("Cannot open input stream for URI"))

            val data = backupData
                ?: return@withContext Result.failure(Exception("Invalid backup file: no manifest found"))

            // Write icon files to disk
            for ((fileName, bytes) in iconFiles) {
                File(iconsDir, fileName).writeBytes(bytes)
            }

            // Restore DB in a single transaction (order matters for FK constraints)
            database.withTransaction {
                // 1. Icons first (products reference them)
                val icons = data.customIcons.map { IconEntity(it.uniqueFileName, it.filePath) }
                if (icons.isNotEmpty()) {
                    database.iconDao().upsertGroceryIcons(icons)
                }

                // 2. Products (groceries reference them)
                val products = data.products.map {
                    ProductEntity(
                        id = it.id,
                        name = it.name,
                        isDefault = it.isDefault,
                        isFavorite = it.isFavorite,
                        iconFileName = it.iconFileName,
                        categoryId = it.categoryId
                    )
                }
                if (products.isNotEmpty()) {
                    database.productDao().upsertProducts(products)
                }

                // 3. Grocery lists (groceries reference them)
                val lists = data.groceryLists.map {
                    GroceryListEntity(it.id, it.name, it.sortingPriority)
                }
                if (lists.isNotEmpty()) {
                    database.groceryListDao().upsertGroceryLists(lists)
                }

                // 4. Groceries last (reference both products and lists)
                val groceries = data.groceries.map {
                    GroceryEntity(
                        productId = it.productId,
                        groceryListId = it.groceryListId,
                        description = it.description,
                        purchased = it.purchased,
                        purchasedLastModified = it.purchasedLastModified
                    )
                }
                if (groceries.isNotEmpty()) {
                    database.groceryDao().upsertGroceries(groceries)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupRestore", "Import failed", e)
            Result.failure(e)
        }
    }
}

