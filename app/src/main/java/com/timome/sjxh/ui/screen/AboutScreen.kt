package com.timome.sjxh.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timome.sjxh.ui.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    updateViewModel: UpdateViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    
    // 检查更新对话框状态
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showNoUpdateDialog by remember { mutableStateOf(false) }
    var showInstallPermissionDialog by remember { mutableStateOf(false) }
    var showDownloadProgress by remember { mutableStateOf(false) }
    
    // 观察更新状态
    val isChecking = updateViewModel.isChecking
    val isUpdateAvailable = updateViewModel.isUpdateAvailable
    val updateInfo = updateViewModel.updateInfo
    val isDownloading = updateViewModel.isDownloading
    val downloadProgress = updateViewModel.downloadProgress
    val errorMessage = updateViewModel.errorMessage
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("关于") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )
        
        // 检查更新卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "检查更新",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        updateViewModel.checkForUpdate()
                    },
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("检查更新")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "学号抽取器",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "作者：timome",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "友情赞助：qwen3-coder-plus",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                Text(
                    text = "版本 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Text(
                    text = "学号随机抽取工具，支持动画抽取和语音播报",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
    
    // 检查更新结果对话框
    if (isUpdateAvailable) {
        showUpdateDialog = true
    }
    
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { 
                showUpdateDialog = false
                updateViewModel.reset()
            },
            title = { Text("发现新版本") },
            text = {
                Column {
                    Text("新版本: ${updateInfo?.version}")
                    Text("更新内容: ${updateInfo?.releaseNotes.takeIf { !it.isNullOrEmpty() } ?: "暂无更新说明"}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                        if (updateViewModel.hasInstallPermission(context)) {
                            updateViewModel.startDownload(context, updateInfo?.downloadUrl ?: "")
                            showDownloadProgress = true
                        } else {
                            showInstallPermissionDialog = true
                        }
                    }
                ) {
                    Text("更新")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                        updateViewModel.reset()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 无更新对话框
    if (showNoUpdateDialog || (!isChecking && !isUpdateAvailable && updateViewModel.updateInfo != null && !showUpdateDialog)) {
        AlertDialog(
            onDismissRequest = { showNoUpdateDialog = false },
            title = { Text("检查更新") },
            text = { Text("未找到更新！") },
            confirmButton = {
                Button(
                    onClick = { showNoUpdateDialog = false }
                ) {
                    Text("确定")
                }
            }
        )
    }
    
    // 安装权限说明对话框
    if (showInstallPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showInstallPermissionDialog = false },
            title = { Text("安装权限") },
            text = { Text("需要安装未知应用权限，请点击确定跳转到设置页面授权") },
            confirmButton = {
                Button(
                    onClick = {
                        updateViewModel.openInstallPermissionSettings(context)
                        showInstallPermissionDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showInstallPermissionDialog = false
                        updateViewModel.cancelDownload()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 下载进度对话框
    if (showDownloadProgress || isDownloading) {
        AlertDialog(
            onDismissRequest = { 
                updateViewModel.cancelDownload()
                showDownloadProgress = false
            },
            title = { Text("下载更新") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("%.2f%%".format(downloadProgress * 100))
                }
            },
            confirmButton = {
                if (isDownloading) {
                    Button(
                        onClick = {
                            updateViewModel.cancelDownload()
                            showDownloadProgress = false
                        }
                    ) {
                        Text("取消")
                    }
                } else {
                    // 下载完成
                    Button(
                        onClick = {
                            showDownloadProgress = false
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        )
    }
    
    // 错误提示对话框
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                updateViewModel.errorMessage = null
            },
            title = { Text("错误") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(
                    onClick = {
                        updateViewModel.errorMessage = null
                    }
                ) {
                    Text("确定")
                }
            }
        )
    }
}