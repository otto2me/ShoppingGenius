package com.rendox.grocerygenius.data.icons

import android.content.Context
import android.util.Log
import com.rendox.grocerygenius.data.Synchronizer
import com.rendox.grocerygenius.data.changeListSync
import com.rendox.grocerygenius.data.model.asEntity
import com.rendox.grocerygenius.data.model.asExternalModel
import com.rendox.grocerygenius.database.groceryicon.IconDao
import com.rendox.grocerygenius.database.groceryicon.IconEntity
import com.rendox.grocerygenius.feature.widget.ActiveGroceryListWidgetProvider
import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.model.IconReference
import com.rendox.grocerygenius.network.data.sources.icon.IconNetworkDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IconRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val iconDao: IconDao,
    private val iconNetworkDataSource: IconNetworkDataSource
) : IconRepository {

    override fun getIconsGroupedByCategory(): Flow<Map<Category, List<IconReference>>> =
        iconDao.getIconsGroupedByCategory().map { map ->
            map.entries.associate { (combinedCategory, icons) ->
                combinedCategory.asExternalModel() to icons
            }
        }

    override suspend fun getGroceryIconsByName(name: String): List<IconReference> = iconDao.getGroceryIconsByName(name)

    override suspend fun deleteIcon(uniqueFileName: String) {
        withContext(Dispatchers.IO) {
            iconDao.deleteIcons(listOf(uniqueFileName))
            val iconFile = File(appContext.filesDir, "icons/$uniqueFileName")
            if (iconFile.exists()) {
                try {
                    iconFile.delete()
                } catch (e: IOException) {
                    Log.w("IconRepository", "Failed to delete icon file: ${iconFile.absolutePath}; ${e.message}")
                }
            }
            ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
        }
    }

    override suspend fun importCustomIconFromUrl(
        imageUrl: String,
        fallbackImageUrl: String?
    ): IconReference? = withContext(Dispatchers.IO) {
        val iconsDir = File(appContext.filesDir, "icons")
        if (!iconsDir.exists()) iconsDir.mkdirs()

        for (sourceUrl in listOf(imageUrl, fallbackImageUrl).filterNotNull().distinct()) {
            val importedIcon = tryImportCustomIconFromSource(
                sourceUrl = sourceUrl,
                iconsDir = iconsDir
            )
            if (importedIcon != null) return@withContext importedIcon
        }

        Log.w("IconRepository", "Failed to import custom icon from all candidate URLs")
        null
    }

    private suspend fun tryImportCustomIconFromSource(
        sourceUrl: String,
        iconsDir: File
    ): IconReference? {
        return try {
            val connection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("User-Agent", REMOTE_IMAGE_USER_AGENT)
                setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            }

            connection.use { httpConnection ->
                val responseCode = httpConnection.responseCode
                if (responseCode !in 200..299) {
                    Log.w("IconRepository", "Remote icon download failed with HTTP $responseCode for $sourceUrl")
                    return null
                }

                val contentType = httpConnection.contentType.orEmpty().substringBefore(';').lowercase()
                if (contentType.startsWith("text/")) {
                    Log.w("IconRepository", "Remote icon download returned text content for $sourceUrl")
                    return null
                }

                val extension = guessExtension(sourceUrl = sourceUrl, contentType = contentType)
                val fileName = "custom_${UUID.randomUUID()}.$extension"
                val targetFile = File(iconsDir, fileName)

                httpConnection.inputStream.use { input ->
                    targetFile.outputStream().use { output -> input.copyTo(output) }
                }

                if (targetFile.length() == 0L) {
                    targetFile.delete()
                    Log.w("IconRepository", "Remote icon download created an empty file for $sourceUrl")
                    return null
                }

                val iconEntity = IconEntity(
                    uniqueFileName = fileName,
                    filePath = "icons/$fileName"
                )
                iconDao.upsertGroceryIcon(iconEntity)
                ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
                IconReference(
                    uniqueFileName = iconEntity.uniqueFileName,
                    filePath = iconEntity.filePath
                )
            }
        } catch (e: Exception) {
            Log.w("IconRepository", "Failed to import custom icon from $sourceUrl: ${e.message}")
            null
        }
    }

    private fun guessExtension(sourceUrl: String, contentType: String): String {
        val extensionFromContentType = when (contentType) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            "image/svg+xml" -> "svg"
            else -> null
        }
        if (extensionFromContentType != null) return extensionFromContentType

        return sourceUrl.substringAfterLast('.', missingDelimiterValue = "")
            .substringBefore('?')
            .lowercase()
            .takeIf { it in setOf("png", "jpg", "jpeg", "webp", "svg") }
            ?: "png"
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = synchronizer.changeListSync(
        prepopulateWithInitialData = {
            val icons = iconNetworkDataSource.downloadIcons()
            iconDao.upsertGroceryIcons(icons.map { it.asEntity() })
        },
        versionReader = { it.iconVersion },
        changeListFetcher = { currentVersion ->
            iconNetworkDataSource.getIconChangeList(after = currentVersion)
        },
        versionUpdater = { latestVersion ->
            copy(iconVersion = latestVersion)
        },
        modelDeleter = { iconIds ->
            iconDao.deleteIcons(iconIds)
            for (fileName in iconIds) {
                val iconFile = File(appContext.filesDir, "icons/$fileName")
                try {
                    iconFile.delete()
                } catch (e: IOException) {
                    Log.w(
                        "IconRepository",
                        "Failed to delete icon: ${iconFile.absolutePath}; ${e.message}"
                    )
                }
            }
            ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
        },
        modelUpdater = { changedIds ->
            val networkIcons = iconNetworkDataSource.downloadIconsByIds(ids = changedIds)
            iconDao.upsertGroceryIcons(networkIcons.map { it.asEntity() })
            ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
        }
    )
}

private inline fun <T> HttpURLConnection.use(block: (HttpURLConnection) -> T): T = try {
    block(this)
} finally {
    disconnect()
}

private const val REMOTE_IMAGE_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
