package com.timome.sjxh.data.repository

import com.timome.sjxh.ui.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/Timome-Sudo/Random-Number-Selection/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseUpdateInfo(response)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseUpdateInfo(jsonString: String): UpdateInfo? {
        return try {
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val tagName = jsonObject["tag_name"]?.jsonPrimitive?.content ?: ""
            val assets = jsonObject["assets"]?.jsonArray
            val downloadUrl = assets?.firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.content?.contains(".apk") == true }
                ?.jsonObject?.get("browser_download_url")?.jsonPrimitive?.content ?: ""
            val releaseNotes = jsonObject["body"]?.jsonPrimitive?.content ?: ""
            val fileSize = assets?.firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.content?.contains(".apk") == true }
                ?.jsonObject?.get("size")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

            if (tagName.isNotEmpty() && downloadUrl.isNotEmpty()) {
                UpdateInfo(
                    version = tagName,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    fileSize = fileSize
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}