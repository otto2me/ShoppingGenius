package com.rendox.shoppinggenius.filestorage

import android.content.Context
import android.util.Log
import com.rendox.shoppinggenius.network.di.Dispatcher
import com.rendox.shoppinggenius.network.di.ShoppingGeniusDispatchers
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class JsonAssetDecoder @Inject constructor(
    @ApplicationContext val appContext: Context,
    @Dispatcher(ShoppingGeniusDispatchers.IO) val ioDispatcher: CoroutineDispatcher
) {
    suspend fun <T> decodeFromFile(
        adapter: JsonAdapter<T>,
        fileName: String
    ): T? = withContext(ioDispatcher) {
        try {
            val json = appContext.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
            adapter.fromJson(json)
        } catch (e: IOException) {
            Log.e("JsonAssetDecoder", "Error reading $fileName: ${e.message}")
            null
        } catch (e: JsonDataException) {
            Log.e("JsonAssetDecoder", "Error parsing $fileName: ${e.message}")
            null
        }
    }
}
