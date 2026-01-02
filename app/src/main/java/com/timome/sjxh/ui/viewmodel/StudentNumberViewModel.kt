package com.timome.sjxh.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.max
import com.timome.sjxh.data.StudentNumberManager
import com.timome.sjxh.data.StudentNumberRange
import com.timome.sjxh.datastore.SettingsDataStore

class StudentNumberViewModel(
    private val numberManager: StudentNumberManager = StudentNumberManager(),
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    
    var startNumber by mutableStateOf("1")
    var endNumber by mutableStateOf("30")
    var allowDuplicates by mutableStateOf(false)  // 默认为false（不允许重复）
    var enableTransitionAnimation by mutableStateOf(true)  // 新增：过渡动画开关，默认开启
    var enableTts by mutableStateOf(false)  // 新增：TTS开关，默认关闭
    var ttsText by mutableStateOf("恭喜%学号号同学成功被抽中！")  // 新增：TTS文本
    var animationDelay by mutableStateOf("10")  // 新增：动画延迟时间（毫秒）
    var isRunning by mutableStateOf(false)
    var isAnimating by mutableStateOf(false)  // 新增：动画状态
    
    init {
        // 从DataStore加载保存的设置
        loadSettings()
    }
    
    private fun loadSettings() {
        settingsDataStore.startNumber.onEach { value ->
            startNumber = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.endNumber.onEach { value ->
            endNumber = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.allowDuplicates.onEach { value ->
            allowDuplicates = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.enableTransitionAnimation.onEach { value ->
            enableTransitionAnimation = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.enableTts.onEach { value ->
            enableTts = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.ttsText.onEach { value ->
            ttsText = value
        }.launchIn(viewModelScope)
        
        settingsDataStore.animationDelay.onEach { value ->
            animationDelay = value
        }.launchIn(viewModelScope)
    }
    
    // 保存所有设置到DataStore
    fun saveSettings() {
        viewModelScope.launch {
            settingsDataStore.saveStartNumber(startNumber)
            settingsDataStore.saveEndNumber(endNumber)
            settingsDataStore.saveAllowDuplicates(allowDuplicates)
            settingsDataStore.saveEnableTransitionAnimation(enableTransitionAnimation)
            settingsDataStore.saveEnableTts(enableTts)
            settingsDataStore.saveTtsText(ttsText)
            settingsDataStore.saveAnimationDelay(animationDelay)
        }
    }
    
    fun setRange() {
        val start = startNumber.toIntOrNull() ?: 1
        val end = endNumber.toIntOrNull() ?: 30
        
        // 确保开始数字小于等于结束数字
        val correctedStart = min(start, end)
        val correctedEnd = max(start, end)
        
        numberManager.setRange(StudentNumberRange(correctedStart, correctedEnd))
        numberManager.setAllowDuplicates(allowDuplicates)
    }
    
    fun startAnimation() {
        // 只设置范围但不重置已使用的数字，保留进度数据
        val start = startNumber.toIntOrNull() ?: 1
        val end = endNumber.toIntOrNull() ?: 30
        
        // 确保开始数字小于等于结束数字
        val correctedStart = kotlin.math.min(start, end)
        val correctedEnd = kotlin.math.max(start, end)
        
        numberManager.setRangeWithoutReset(StudentNumberRange(correctedStart, correctedEnd))
        numberManager.setAllowDuplicates(allowDuplicates)
        numberManager.startAnimation()
        isAnimating = true
        isRunning = true
    }
    
    fun stopAnimation(): Int? {
        val result = numberManager.stopAnimation()
        isAnimating = false
        return result
    }
    
    fun getRandomNumberForAnimation(): Int {
        return numberManager.getRandomNumberForAnimation()
    }
    
    fun reset() {
        numberManager.reset()
        isRunning = false
        isAnimating = false
    }
    
    fun canContinue(): Boolean {
        return numberManager.canContinue()
    }
    
    fun getProgress(): Float {
        return numberManager.getProgress()
    }
    
    fun getUsedNumbersCount(): Int {
        return numberManager.getUsedNumbersCount()
    }
    
    fun getTotalPossibleNumbers(): Int {
        return numberManager.getTotalPossibleNumbers()
    }
    
    fun isAnimationRunning(): Boolean {
        return numberManager.isAnimationRunning()
    }
    
    // 获取每次抽取的固定百分比
    fun getFixedPercentage(): Float {
        return numberManager.getFixedPercentage()
    }
    
    // 获取当前百分比（根据是否允许重复而变化）
    fun getCurrentPercentage(): Float {
        return numberManager.getCurrentPercentage()
    }
    
    // 获取格式化的固定百分比字符串
    fun getFormattedFixedPercentageString(): String {
        return numberManager.getFormattedFixedPercentageString()
    }
    
    // 获取格式化的当前百分比字符串
    fun getFormattedCurrentPercentageString(): String {
        return numberManager.getFormattedCurrentPercentageString()
    }
    
    // 验证输入数据是否有效
    fun isValidInput(): Boolean {
        // 检查开始数字是否为空
        if (startNumber.isEmpty()) {
            return false
        }
        
        // 检查结束数字是否为空
        if (endNumber.isEmpty()) {
            return false
        }
        
        // 尝试将输入转换为整数
        val start = startNumber.toIntOrNull()
        val end = endNumber.toIntOrNull()
        
        // 检查是否为有效数字
        if (start == null || end == null) {
            return false
        }
        
        // 检查开始数字不能大于结束数字
        if (start > end) {
            return false
        }
        
        return true
    }
    
    // 获取验证错误信息
    fun getValidationError(): String {
        // 检查开始数字是否为空
        if (startNumber.isEmpty()) {
            return "未输入开始数字！"
        }
        
        // 检查结束数字是否为空
        if (endNumber.isEmpty()) {
            return "未输入结束数字！"
        }
        
        // 尝试将输入转换为整数
        val start = startNumber.toIntOrNull()
        val end = endNumber.toIntOrNull()
        
        // 检查是否为有效数字
        if (start == null) {
            return "开始数字必须是数字！"
        }
        
        if (end == null) {
            return "结束数字必须是数字！"
        }
        
        // 检查开始数字不能大于结束数字
        if (start > end) {
            return "开始数字不能大于结束数字！"
        }
        
        return ""
    }
    
    // 获取动画延迟时间（毫秒）
    fun getAnimationDelay(): Long {
        val delay = animationDelay.toIntOrNull()
        return if (delay != null && delay > 0) delay.toLong() else 10L
    }
    
    fun resetTtsText() {
        ttsText = "恭喜%学号号同学成功被抽中！"
        // 保存到DataStore
        viewModelScope.launch {
            settingsDataStore.saveTtsText(ttsText)
        }
    }
    
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun create(numberManager: StudentNumberManager? = null, settingsDataStore: SettingsDataStore): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    return StudentNumberViewModel(numberManager ?: StudentNumberManager(), settingsDataStore) as T
                }
                
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentNumberViewModel(numberManager ?: StudentNumberManager(), settingsDataStore) as T
                }
            }
    }
}