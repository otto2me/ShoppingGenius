package com.rendox.shoppinggenius.network.data.sources.icon

import android.content.Context
import android.util.Log
import com.rendox.shoppinggenius.filestorage.LocalAssetDataLoader
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.network.model.NetworkChangeList
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class OfflineFirstIconNetworkDataSource @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val localAssetDataLoader: LocalAssetDataLoader
) : IconNetworkDataSource {

    override suspend fun downloadIcons(): List<IconReference> {
        // Copy icons from assets to filesDir
        return try {
            val iconFiles = appContext.assets.list("icons")
                ?.filter { it.endsWith(".png") && !it.startsWith("all_icons") }
                ?: emptyList()

            val iconsDir = appContext.filesDir.resolve("icons").apply { mkdirs() }

            iconFiles.map { fileName ->
                try {
                    val targetFile = iconsDir.resolve(fileName)
                    appContext.assets.open("icons/$fileName").use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    IconReference(
                        uniqueFileName = fileName,
                        filePath = targetFile.toRelativeString(appContext.filesDir)
                    )
                } catch (e: IOException) {
                    Log.w("IconNetworkDataSource", "Failed to copy icon $fileName: ${e.message}")
                    null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            Log.e("IconNetworkDataSource", "Error downloading icons from assets: ${e.message}")
            emptyList()
        }
    }

    override suspend fun downloadIconsByIds(ids: List<String>): List<IconReference> = ids.mapNotNull { iconName ->
        try {
            val iconFile = appContext.filesDir.resolve("icons/$iconName")
                .apply { parentFile?.mkdirs() }
            appContext.assets.open("icons/$iconName").use { input ->
                iconFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            IconReference(
                uniqueFileName = iconName,
                filePath = iconFile.toRelativeString(appContext.filesDir)
            )
        } catch (e: IOException) {
            Log.w("IconNetworkDataSource", "Failed to copy icon $iconName: ${e.message}")
            null
        }
    }

    override suspend fun getIconChangeList(after: Int): List<NetworkChangeList> =
        localAssetDataLoader.loadIconsChangeList().filter { it.changeListVersion > after }
}
