package com.rendox.shoppinggenius.feature.share

import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rendox.shoppinggenius.data.category.CategoryRepository
import com.rendox.shoppinggenius.data.grocery.GroceryRepository
import com.rendox.shoppinggenius.data.grocerylist.GroceryListRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.GroceryList
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.zip.GZIPInputStream
import android.content.Context
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private const val IMPORT_PATH_PREFIX = "i"
private const val APP_IMPORT_SCHEME = "shoppinggenius"
private const val APP_IMPORT_HOST = "import"
private const val IMPORT_QUERY_PAYLOAD = "payload"
private const val SHARE_VERSION = 1
private const val MAX_SHARED_ITEMS = 200
const val SHARE_FILE_MIME_TYPE = "application/vnd.shoppinggenius.list+json"

class GroceryListShareManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val groceryListRepository: GroceryListRepository,
    private val groceryRepository: GroceryRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    data class ShareContent(
        val text: String,
        val fileName: String,
        val fileContent: String,
        val fileMimeType: String = SHARE_FILE_MIME_TYPE
    )

    fun buildShareContent(listName: String, groceries: List<Grocery>): ShareContent {
        val normalizedName = listName.ifBlank { "Shopping list" }
        val shareableItems = groceries
            .filterNot { it.purchased }
            .sortedBy { it.name.lowercase() }
            .take(MAX_SHARED_ITEMS)

        val fileContent = buildPayloadJson(
            listName = normalizedName,
            groceries = shareableItems
        )
        val lines = shareableItems.joinToString(separator = "\n") { grocery ->
            val description = grocery.description?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
            "- ${grocery.name}$description"
        }

        val text = buildString {
            append(normalizedName)
            append("\n")
            if (lines.isNotBlank()) {
                append(lines)
            }
        }
        val fileName = normalizedName
            .replace(Regex("[^a-zA-Z0-9._-]+"), "_")
            .ifBlank { "shopping_list" } + ".sglist"
        return ShareContent(
            text = text,
            fileName = fileName,
            fileContent = fileContent
        )
    }

    suspend fun importFromUri(uri: Uri): ImportResult {
        val parsedPayload = when {
            uri.scheme == "content" || uri.scheme == "file" -> {
                val contentText = appContext.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?: return ImportResult.Error("missing_payload")
                parsePayload(contentText)
            }

            else -> {
                val payload = extractPayload(uri) ?: return ImportResult.Error("missing_payload")
                val decodedJson = decodePayload(payload) ?: return ImportResult.Error("invalid_payload")
                parsePayload(decodedJson)
            }
        } ?: return ImportResult.Error("invalid_json")

        val finalListName = parsedPayload.listName.ifBlank { "Imported list" }
        val importListName = "$finalListName (Import)"
        val existingImportList = groceryListRepository.getAllGroceryLists().first()
            .firstOrNull { it.name == importListName }
        val targetList = existingImportList ?: GroceryList(
            id = UUID.randomUUID().toString(),
            name = importListName
        )
        if (existingImportList == null) {
            groceryListRepository.insertGroceryList(targetList)
        } else {
            groceryListRepository.updateGroceryListName(targetList.id, importListName)
            groceryRepository.removeGroceriesFromList(targetList.id)
        }

        val categoriesByName = categoryRepository.getAllCategories().first()
            .associateBy { it.name.trim().lowercase() }

        var importedItemsCount = 0
        parsedPayload.items.forEach { item ->
            val name = item.name.trim()
            if (name.isBlank()) return@forEach

            val matchedCategoryId = item.categoryName
                ?.trim()
                ?.lowercase()
                ?.let { categoriesByName[it]?.id }

            val existingProductId = productRepository.getProductsByName(name)
                .firstOrNull()
                ?.id

            if (existingProductId != null) {
                groceryRepository.addGroceryToList(
                    productId = existingProductId,
                    listId = targetList.id,
                    description = item.description,
                    purchased = item.purchased
                )
            } else {
                groceryRepository.insertProductAndGrocery(
                    name = name,
                    productId = UUID.randomUUID().toString(),
                    iconId = null,
                    categoryId = matchedCategoryId,
                    groceryListId = targetList.id,
                    description = item.description,
                    purchased = item.purchased,
                    isDefault = false
                )
            }
            importedItemsCount++
        }

        return ImportResult.Success(targetList.id, targetList.name, importedItemsCount)
    }

    private fun buildPayloadJson(listName: String, groceries: List<Grocery>): String {
        val json = JSONObject().apply {
            put("v", SHARE_VERSION)
            put("listName", listName)
            put(
                "items",
                JSONArray().apply {
                    groceries.forEach { grocery ->
                        put(
                            JSONObject().apply {
                                put("name", grocery.name)
                                put("description", grocery.description)
                                put("purchased", grocery.purchased)
                                put("categoryName", grocery.category?.name)
                            }
                        )
                    }
                }
            )
        }
        return json.toString()
    }


    private fun parsePayload(jsonText: String): ParsedPayload? {
        return runCatching {
            val root = JSONObject(jsonText)
            val itemsJson = root.optJSONArray("items") ?: JSONArray()
            val items = buildList {
                for (i in 0 until itemsJson.length()) {
                    val item = itemsJson.optJSONObject(i) ?: continue
                    add(
                        ParsedItem(
                            name = item.optString("name", ""),
                            description = item.optString("description", "").ifBlank { null },
                            purchased = item.optBoolean("purchased", false),
                            categoryName = item.optString("categoryName", "").ifBlank { null }
                        )
                    )
                }
            }
            ParsedPayload(
                listName = root.optString("listName", "Imported list"),
                items = items
            )
        }.getOrNull()
    }


    private fun decodePayload(encoded: String): String? {
        return runCatching {
            val bytes = Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP)
            GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull()
    }

    private fun extractPayload(uri: Uri): String? {
        // Accept multiple formats for compatibility:
        // 1) https://shoppinggenius.app/i/<payload>
        // 2) shoppinggenius://import?payload=<payload>
        // 3) https://shoppinggenius.app/import?payload=<payload> (legacy)
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2 && pathSegments.firstOrNull() == IMPORT_PATH_PREFIX) {
            return pathSegments[1].takeIf { it.isNotBlank() }
        }
        if (uri.scheme == APP_IMPORT_SCHEME && uri.host == APP_IMPORT_HOST) {
            return pathSegments.firstOrNull()?.takeIf { it.isNotBlank() }
        }
        return uri.getQueryParameter(IMPORT_QUERY_PAYLOAD)?.takeIf { it.isNotBlank() }
    }

    data class ParsedPayload(
        val listName: String,
        val items: List<ParsedItem>
    )

    data class ParsedItem(
        val name: String,
        val description: String?,
        val purchased: Boolean,
        val categoryName: String?
    )

    sealed interface ImportResult {
        data class Success(val listId: String, val listName: String, val importedItems: Int) : ImportResult
        data class Error(val reason: String) : ImportResult
    }
}

