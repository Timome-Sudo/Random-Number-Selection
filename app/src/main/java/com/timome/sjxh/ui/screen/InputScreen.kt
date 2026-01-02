package com.timome.sjxh.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.timome.sjxh.ui.components.DraggableSwitch
import com.timome.sjxh.ui.viewmodel.StudentNumberViewModel
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.net.Uri
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: StudentNumberViewModel,
    onStart: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    var ttsAvailable by remember { mutableStateOf(false) }
    var showTtsResetDialog by remember { mutableStateOf(false) }
    
    // 检查TTS是否可用
    LaunchedEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            ttsAvailable = status == TextToSpeech.SUCCESS
            if (status == TextToSpeech.SUCCESS) {
                // 检查是否有可用的语言
                val result = tts?.isLanguageAvailable(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsAvailable = false
                }
            }
        }
        tts?.shutdown() // 不需要实际使用，只需检查
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("学号抽取器") },
            actions = {
                IconButton(onClick = onNavigateToAbout) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Info,
                        contentDescription = "关于"
                    )
                }
            }
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  // 让Card占据剩余空间
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            ScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 开始数字输入
                OutlinedTextField(
                    value = viewModel.startNumber,
                    onValueChange = { newValue ->
                        // 只允许输入数字
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            viewModel.startNumber = newValue
                            // 保存设置
                            viewModel.saveSettings()
                        }
                    },
                    label = { Text("开始学号") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    isError = viewModel.startNumber.isEmpty() || 
                        (viewModel.endNumber.isNotEmpty() && 
                         viewModel.startNumber.isNotEmpty() && 
                         (viewModel.startNumber.toIntOrNull() ?: 0) > (viewModel.endNumber.toIntOrNull() ?: 0)),
                    supportingText = {
                        if (viewModel.startNumber.isEmpty()) {
                            Text("未输入数字！", color = MaterialTheme.colorScheme.error)
                        } else if (viewModel.endNumber.isNotEmpty() && 
                                   viewModel.startNumber.isNotEmpty() && 
                                   (viewModel.startNumber.toIntOrNull() ?: 0) > (viewModel.endNumber.toIntOrNull() ?: 0)) {
                            Text("开始数字不能大于结束数字！", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )
                
                // 结束数字输入
                OutlinedTextField(
                    value = viewModel.endNumber,
                    onValueChange = { newValue ->
                        // 只允许输入数字
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            viewModel.endNumber = newValue
                            // 保存设置
                            viewModel.saveSettings()
                        }
                    },
                    label = { Text("结束学号") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    isError = viewModel.endNumber.isEmpty() || 
                        (viewModel.startNumber.isNotEmpty() && 
                         viewModel.endNumber.isNotEmpty() && 
                         (viewModel.endNumber.toIntOrNull() ?: 0) < (viewModel.startNumber.toIntOrNull() ?: 0)),
                    supportingText = {
                        if (viewModel.endNumber.isEmpty()) {
                            Text("未输入数字！", color = MaterialTheme.colorScheme.error)
                        } else if (viewModel.startNumber.isNotEmpty() && 
                                   viewModel.endNumber.isNotEmpty() && 
                                   (viewModel.endNumber.toIntOrNull() ?: 0) < (viewModel.startNumber.toIntOrNull() ?: 0)) {
                            Text("开始数字不能大于结束数字！", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )
                
                // 重复开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "允许重复抽取",  // 根据要求保持标签不变
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = viewModel.allowDuplicates,
                        onCheckedChange = { 
                            viewModel.allowDuplicates = it
                            // 保存设置
                            viewModel.saveSettings()
                        }
                    )
                }
                
                // 过渡动画开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "过渡动画",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = viewModel.enableTransitionAnimation,
                        onCheckedChange = { 
                            viewModel.enableTransitionAnimation = it
                            // 保存设置
                            viewModel.saveSettings()
                        }
                    )
                }
                
                // 显示动画延迟时间输入框（仅在启用过渡动画时）
                AnimatedVisibility(
                    visible = viewModel.enableTransitionAnimation,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        // 动画延迟时间输入
                        OutlinedTextField(
                            value = viewModel.animationDelay,
                            onValueChange = { newValue ->
                                // 只允许输入数字
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    viewModel.animationDelay = newValue
                                    // 保存设置
                                    viewModel.saveSettings()
                                }
                            },
                            label = { Text("数字变化时间（毫秒）") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true
                        )
                    }
                }
                
                // TTS语音播报开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "语音播报",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = viewModel.enableTts,
                        onCheckedChange = { 
                            if (ttsAvailable) {
                                viewModel.enableTts = it
                                // 保存设置
                                viewModel.saveSettings()
                            }
                        },
                        enabled = ttsAvailable
                    )
                }
                
                // TTS语法提示
                AnimatedVisibility(
                    visible = viewModel.enableTts,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = "语法提示: %学号=学号, %y=年, %m=月, %d=日, %h=时, %M=分, %s=秒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                
                // TTS设置按钮
                AnimatedVisibility(
                    visible = viewModel.enableTts,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent("com.android.settings.TTS_SETTINGS")
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // 如果无法跳转到TTS设置，显示提示
                                    // 由于在Composable中无法直接显示Toast，我们可以通过其他方式处理
                                }
                            }
                        ) {
                            Text("修改TTS设置")
                        }
                        
                        // 还原TTS文本按钮
                        Button(
                            onClick = { showTtsResetDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("还原文本")
                        }
                    }
                }
                
                // 自定义播报文本
                AnimatedVisibility(
                    visible = viewModel.enableTts,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = viewModel.ttsText,
                        onValueChange = { 
                            viewModel.ttsText = it
                            // 保存设置
                            viewModel.saveSettings()
                        },
                        label = { Text("播报文本") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        enabled = viewModel.enableTts,
                        singleLine = true
                    )
                }
                
                Button(
                    onClick = {
                        if (viewModel.isValidInput()) {
                            viewModel.setRange()
                            onStart()
                        }
                        // 如果输入无效，按钮点击不会执行任何操作，错误信息已经在输入框下方显示
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),  // 添加顶部间距
                    enabled = viewModel.isValidInput()  // 根据验证结果启用/禁用按钮
                ) {
                    Text(
                        text = "开始抽取",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // TTS文本还原确认对话框
    if (showTtsResetDialog) {
        AlertDialog(
            onDismissRequest = { showTtsResetDialog = false },
            title = { Text("还原TTS文本") },
            text = { Text("确定要还原到默认文本吗？") },
            confirmButton = {
                Button(  // 修改为Button以使用与还原TTS文本按钮相同的颜色
                    onClick = {
                        viewModel.resetTtsText()
                        showTtsResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(  // 保持取消按钮为TextButton，使用默认颜色
                    onClick = { showTtsResetDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        content = {
            item {
                content()
            }
        }
    )
}