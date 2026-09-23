package com.shoppinggenius.app.feature.carapp

import android.graphics.BitmapFactory
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.shoppinggenius.app.model.IconReference
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CarAppIconLoader(
    private val filesDir: File,
    private val coroutineScope: CoroutineScope,
    private val onIconLoaded: () -> Unit
) {
    private val iconCache = mutableMapOf<String, CarIcon?>()
    private val loadingPaths = mutableSetOf<String>()

    fun preload(iconReferences: Iterable<IconReference?>) {
        iconReferences.forEach(::getOrLoad)
    }

    fun getOrLoad(iconReference: IconReference?): CarIcon? {
        val filePath = iconReference?.filePath ?: return null
        if (iconCache.containsKey(filePath)) {
            return iconCache[filePath]
        }
        if (!loadingPaths.add(filePath)) {
            return null
        }

        coroutineScope.launch {
            val icon = loadIcon(filePath)
            loadingPaths.remove(filePath)
            if (!iconCache.containsKey(filePath) || iconCache[filePath] !== icon) {
                iconCache[filePath] = icon
                onIconLoaded()
            }
        }

        return null
    }

    private suspend fun loadIcon(filePath: String): CarIcon? = withContext(Dispatchers.IO) {
        val iconFile = File(filesDir, filePath)
        if (!iconFile.exists()) return@withContext null

        val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath) ?: return@withContext null
        CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
    }
}

