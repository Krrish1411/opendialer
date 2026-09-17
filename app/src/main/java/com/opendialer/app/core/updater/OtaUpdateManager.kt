package com.opendialer.app.core.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.opendialer.app.BuildConfig
import com.opendialer.app.core.common.Constants
import com.opendialer.app.data.model.UpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtaUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(Constants.GITHUB_API_LATEST_RELEASE)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "OpenDialer-Android")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext UpdateInfo(
                        hasUpdate = false,
                        latestVersionName = BuildConfig.VERSION_NAME,
                        downloadUrl = null,
                        changelog = null,
                        errorMessage = "Server returned ${response.code}"
                    )
                }

                val body = response.body?.string() ?: return@withContext UpdateInfo(
                    hasUpdate = false,
                    latestVersionName = BuildConfig.VERSION_NAME,
                    downloadUrl = null,
                    changelog = null
                )

                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val bodyText = json.optString("body", "Bug fixes and improvements")
                val assets = json.optJSONArray("assets")

                var apkDownloadUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkDownloadUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }

                val currentVersion = BuildConfig.VERSION_NAME
                val isNewer = isVersionNewer(tagName, currentVersion)

                UpdateInfo(
                    hasUpdate = isNewer,
                    latestVersionName = tagName.ifEmpty { currentVersion },
                    downloadUrl = apkDownloadUrl,
                    changelog = bodyText
                )
            }
        } catch (e: Exception) {
            UpdateInfo(
                hasUpdate = false,
                latestVersionName = BuildConfig.VERSION_NAME,
                downloadUrl = null,
                changelog = null,
                errorMessage = e.localizedMessage ?: "Failed to connect to GitHub"
            )
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank()) return false
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    suspend fun downloadAndInstallApk(
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(downloadUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val body = response.body ?: return@withContext false

                val apkDir = File(context.cacheDir, "updates")
                if (!apkDir.exists()) apkDir.mkdirs()
                val apkFile = File(apkDir, "opendialer_update.apk")

                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                body.byteStream().use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                onProgress(downloadedBytes.toFloat() / totalBytes)
                            }
                        }
                    }
                }

                // Launch package installer using FileProvider for in-place seamless upgrade
                withContext(Dispatchers.Main) {
                    val apkUri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
