package com.timome.sjxh.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timome.sjxh.data.repository.UpdateRepository
import com.timome.sjxh.ui.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updateRepository = UpdateRepository()
    private val context = application
    
    var updateInfo by mutableStateOf<UpdateInfo?>(null)
        internal set
    var isChecking by mutableStateOf(false)
        internal set
    var isUpdateAvailable by mutableStateOf(false)
        internal set
    var isDownloading by mutableStateOf(false)
        internal set
    var downloadProgress by mutableStateOf(0f)
        internal set
    var isDownloadComplete by mutableStateOf(false)
        internal set
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    var downloadUrl by mutableStateOf<String?>(null)
        internal set

    fun checkForUpdate() {
        viewModelScope.launch {
            isChecking = true
            errorMessage = null
            
            try {
                val latestUpdate = updateRepository.checkForUpdate()
                updateInfo = latestUpdate
                
                if (latestUpdate != null) {
                    val currentVersion = getCurrentAppVersion()
                    isUpdateAvailable = isNewerVersion(latestUpdate.version, currentVersion)
                } else {
                    isUpdateAvailable = false
                }
            } catch (e: Exception) {
                errorMessage = e.message
                e.printStackTrace()
            } finally {
                isChecking = false
            }
        }
    }

    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        return try {
            val newVersionNumbers = newVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
            val currentVersionNumbers = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            
            for (i in 0 until maxOf(newVersionNumbers.size, currentVersionNumbers.size)) {
                val newNum = if (i < newVersionNumbers.size) newVersionNumbers[i] else 0
                val currentNum = if (i < currentVersionNumbers.size) currentVersionNumbers[i] else 0
                
                if (newNum > currentNum) return true
                if (newNum < currentNum) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    fun startDownload(context: Context, downloadUrl: String) {
        viewModelScope.launch {
            isDownloading = true
            downloadProgress = 0f
            isDownloadComplete = false
            this@UpdateViewModel.downloadUrl = downloadUrl
            errorMessage = null
            
            try {
                downloadFile(context, downloadUrl)
            } catch (e: Exception) {
                errorMessage = e.message
                e.printStackTrace()
            } finally {
                isDownloading = false
            }
        }
    }
    
    private suspend fun downloadFile(context: Context, downloadUrl: String) = withContext(Dispatchers.IO) {
        var connection: HttpsURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        
        try {
            val url = URL(downloadUrl)
            connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            val fileLength = connection.contentLength
            
            val fileName = "update.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            inputStream = connection.inputStream
            outputStream = FileOutputStream(file)
            
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            
            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toFloat()
                    downloadProgress = progress / 100
                }
                
                outputStream.write(data, 0, count)
                
                // Check if download was cancelled
                if (!isDownloading) break
            }
            
            outputStream.flush()
            isDownloadComplete = true
            
            // Install APK after download
            if (isDownloadComplete) {
                installApk(context, file)
            }
        } finally {
            inputStream?.close()
            outputStream?.close()
            connection?.disconnect()
        }
    }
    
    private fun installApk(context: Context, apkFile: File) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                
                val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                } else {
                    Uri.fromFile(apkFile)
                }
                
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                
                context.startActivity(intent)
            } catch (e: Exception) {
                errorMessage = "安装失败: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    fun hasInstallPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Android 8.0以下默认允许安装未知应用
        }
    }
    
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    
    fun cancelDownload() {
        isDownloading = false
        downloadProgress = 0f
        isDownloadComplete = false
    }
    
    fun reset() {
        isChecking = false
        isUpdateAvailable = false
        isDownloading = false
        downloadProgress = 0f
        isDownloadComplete = false
        errorMessage = null
        downloadUrl = null
    }
}