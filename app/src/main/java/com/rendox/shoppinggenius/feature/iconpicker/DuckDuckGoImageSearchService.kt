package com.rendox.shoppinggenius.feature.iconpicker

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Singleton
class DuckDuckGoImageSearchService @Inject constructor() {

    suspend fun searchImages(query: String, maxResults: Int = 8): List<DuckDuckGoImageSearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val vqd = fetchVqdToken(trimmed) ?: return@withContext emptyList()
                val encodedQuery = Uri.encode(trimmed)
                val encodedVqd = Uri.encode(vqd)
                val responseText = getResponseText(
                    url = "https://duckduckgo.com/i.js?l=wt-wt&o=json&q=$encodedQuery&vqd=$encodedVqd&f=,,,&p=1",
                    referer = "https://duckduckgo.com/?q=$encodedQuery&iax=images&ia=images",
                    accept = "application/json, text/javascript, */*; q=0.01"
                )
                parseDuckDuckGoImageResults(responseText).take(maxResults)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun testConnection(): Boolean {
        return searchImages(query = "apple icon", maxResults = 1).isNotEmpty()
    }

    private fun fetchVqdToken(query: String): String? {
        val encodedQuery = Uri.encode(query)
        val responseText = getResponseText(
            url = "https://duckduckgo.com/?q=$encodedQuery&iax=images&ia=images",
            referer = "https://duckduckgo.com/"
        )
        return extractDuckDuckGoVqd(responseText)
    }

    private fun getResponseText(
        url: String,
        referer: String,
        accept: String = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", DUCKDUCKGO_USER_AGENT)
            setRequestProperty("Accept", accept)
            setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            setRequestProperty("Referer", referer)
        }
        return connection.useResponseText()
    }
}

internal fun extractDuckDuckGoVqd(responseText: String): String? {
    val patterns = listOf(
        Regex("vqd=\\\"([^\\\"]+)\\\""),
        Regex("vqd=\\'([^\\']+)\\'"),
        Regex("vqd=([^&\\\"']+)")
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        pattern.find(responseText)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
    }
}

internal fun parseDuckDuckGoImageResults(responseText: String): List<DuckDuckGoImageSearchResult> {
    val root = JSONObject(responseText)
    val items = root.optJSONArray("results") ?: JSONArray()
    return buildList {
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val imageUrl = item.optString("image")
            val title = item.optString("title")
            val thumbnailUrl = item.optString("thumbnail")
            if (imageUrl.isBlank() || thumbnailUrl.isBlank()) continue
            add(
                DuckDuckGoImageSearchResult(
                    title = title,
                    imageUrl = imageUrl,
                    thumbnailUrl = thumbnailUrl
                )
            )
        }
    }
}

private fun HttpURLConnection.useResponseText(): String = try {
    val stream = if (responseCode in 200..299) inputStream else errorStream
    stream?.bufferedReader()?.use { it.readText() }.orEmpty()
} finally {
    disconnect()
}

private const val DUCKDUCKGO_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"



