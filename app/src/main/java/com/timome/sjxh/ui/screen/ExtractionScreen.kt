package com.timome.sjxh.ui.screen

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timome.sjxh.ui.viewmodel.StudentNumberViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ExtractionScreen(
    viewModel: StudentNumberViewModel,
    onBackToInput: () -> Unit
) {
    val context = LocalContext.current
    var currentNumber by remember { mutableStateOf<Int?>(null) }
    var isAnimationRunning by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("开始抽取") }
    var showConfetti by remember { mutableStateOf(false) }  // 确认动画状态
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var showAllExtractedDialog by remember { mutableStateOf(false) } // 新增：显示"全部都抽完了"对话框
    
    // 添加一个DisposableEffect来在组件销毁时重置数据
    DisposableEffect(Unit) {
        onDispose {
            // 在离开此屏幕时重置数据
            viewModel.reset()
        }
    }
    
    // 创建一个状态来存储当前进度，用于实现丝滑动画
    // 在组件创建时就初始化进度值，确保进入界面时进度条自动确定当前值和最大值
    var currentProgress by remember { mutableStateOf(viewModel.getProgress()) }
    
    // 当进入抽取界面时，确保进度条的值被正确初始化
    LaunchedEffect(Unit) {
        currentProgress = viewModel.getProgress()
    }
    
    // 每1毫秒更新一次UI状态，实现丝滑的进度条动画
    LaunchedEffect(isAnimationRunning) {
        while (isAnimationRunning) {
            currentProgress = viewModel.getProgress()
            delay(1) // 每1毫秒更新一次，实现丝滑效果
        }
    }
    
    // 检查是否所有学号都已抽取完（仅在不允许重复时）
    LaunchedEffect(isAnimationRunning) {
        while (isAnimationRunning || !viewModel.allowDuplicates) {
            if (!viewModel.allowDuplicates && viewModel.getUsedNumbersCount() >= viewModel.getTotalPossibleNumbers() && viewModel.getTotalPossibleNumbers() > 0) {
                // 所有学号已抽取完，停止动画并显示对话框
                if (isAnimationRunning) {
                    val result = if (!viewModel.enableTransitionAnimation) {
                        viewModel.getRandomNumberForAnimation()
                    } else {
                        viewModel.stopAnimation()
                    }
                    if (result != null) {
                        currentNumber = result
                    }
                    isAnimationRunning = false
                    buttonText = "开始抽取"
                }
                showAllExtractedDialog = true
            }
            delay(100) // 每100毫秒检查一次
        }
    }
    
    // 初始化TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // TTS初始化失败，禁用TTS功能
                viewModel.enableTts = false
            }
        }
    }
    
    // 确认动画的协程
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(3000) // 显示3秒确认动画
            showConfetti = false
        }
    }
    
    // 动画效果的协程 - 使用ViewModel中的动画延迟设置，仅在启用过渡动画时运行
    LaunchedEffect(isAnimationRunning) {
        while (isAnimationRunning) {
            if (viewModel.isAnimationRunning()) {
                // 根据过渡动画设置决定是否更新动画数字
                if (viewModel.enableTransitionAnimation) {
                    // 更新动画数字
                    currentNumber = viewModel.getRandomNumberForAnimation()
                }
            }
            if (viewModel.enableTransitionAnimation) {
                delay(viewModel.getAnimationDelay()) // 使用自定义延迟时间
            } else {
                delay(10) // 短暂延迟以避免无限循环
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部进度条 - 仅在不允许重复时显示
            if (!viewModel.allowDuplicates) {
                // 使用animateFloatAsState创建平滑的进度动画
                val smoothProgress by animateFloatAsState(
                    targetValue = currentProgress,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 50), // 50ms的快速平滑过渡，实现丝滑效果
                    label = "progress animation"
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "抽取进度",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LinearProgressIndicator(
                            progress = smoothProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${viewModel.getUsedNumbersCount()} / ${viewModel.getTotalPossibleNumbers()} 已抽取",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 百分比显示框 - 总是显示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (viewModel.allowDuplicates) {
                            "单次抽取概率: ${viewModel.getFormattedFixedPercentageString()}"
                        } else {
                            "当前抽取概率: ${viewModel.getFormattedCurrentPercentageString()}"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // 显示当前抽取的学号
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "当前学号",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentNumber?.toString() ?: "—",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 按钮区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (!isAnimationRunning) {
                            // 开始动画
                            viewModel.startAnimation()
                            isAnimationRunning = true
                            buttonText = "停止抽取"
                        } else {
                            // 停止动画并获取实际抽取的数字
                            // 按照要求：先把进度条的进度加起来，再更新进度条状态，最后更新概率除数
                            val result = if (!viewModel.enableTransitionAnimation) {
                                // 如果没有启用过渡动画，直接获取一个数字
                                viewModel.getRandomNumberForAnimation()
                            } else {
                                // 先获取结果（这会把进度条的进度加起来）
                                val stopResult = viewModel.stopAnimation()
                                // 这里隐含了进度的更新，因为stopAnimation()会更新已使用的数字集合
                                
                                // 然后更新进度条状态
                                isAnimationRunning = false
                                
                                stopResult
                            }
                            
                            if (result != null) {
                                currentNumber = result
                                
                                // 启动Material You风格的确认动画
                                showConfetti = true
                                
                                // 如果启用了TTS，则进行语音播报
                                if (viewModel.enableTts) {
                                    val ttsText = viewModel.ttsText
                                        .replace("%学号", result.toString())
                                        .replace("%y", Calendar.getInstance().get(Calendar.YEAR).toString())
                                        .replace("%m", (Calendar.getInstance().get(Calendar.MONTH) + 1).toString().padStart(2, '0'))
                                        .replace("%d", Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0'))
                                        .replace("%h", Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0'))
                                        .replace("%M", Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0'))  // 使用%M表示分钟
                                        .replace("%s", Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2, '0'))
                                    
                                    tts?.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
                                }
                            }
                            // 最后更新按钮文本
                            buttonText = "开始抽取"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        // 在返回输入界面前重置数据
                        viewModel.reset()
                        onBackToInput()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "返回输入界面",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 显示"全部都抽完了！"对话框
        if (showAllExtractedDialog) {
            AlertDialog(
                onDismissRequest = { /* 不允许通过点击外部关闭 */ },
                title = { Text(text = "提示") },
                text = { Text(text = "全部都抽完了！") },
                confirmButton = {
                    Button(
                        onClick = {
                            showAllExtractedDialog = false
                            onBackToInput() // 点击确认后返回输入界面
                        }
                    ) {
                        Text("确认")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}