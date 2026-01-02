package com.timome.sjxh.data

import kotlin.random.Random

data class StudentNumberRange(
    val start: Int,
    val end: Int
)

class StudentNumberManager {
    private var currentRange: StudentNumberRange = StudentNumberRange(1, 30)
    private var allowDuplicates: Boolean = false  // 默认为false，不允许重复
    private var usedNumbers: MutableSet<Int> = mutableSetOf()
    private var isRunning: Boolean = false
    private var isAnimating: Boolean = false  // 新增：是否处于动画模式
    
    fun setRange(range: StudentNumberRange) {
        currentRange = range
        reset()
    }
    
    // 设置范围但不重置已使用的数字，保留进度数据
    fun setRangeWithoutReset(range: StudentNumberRange) {
        currentRange = range
        // 不重置进度数据，保留usedNumbers、isRunning和isAnimating状态
    }
    
    fun setAllowDuplicates(allow: Boolean) {
        allowDuplicates = allow
        if (!allow) {
            // 如果不允许重复，但当前已抽取的数字超出了范围，则需要重置
            val totalPossible = currentRange.end - currentRange.start + 1
            if (usedNumbers.size > totalPossible) {
                reset()
            }
        }
    }
    
    fun reset() {
        usedNumbers.clear()
        isRunning = false
        isAnimating = false
    }
    
    fun startAnimation() {
        isAnimating = true
    }
    
    fun stopAnimation(): Int? {
        isAnimating = false
        return getRandomNumberForReal()
    }
    
    private fun getRandomNumberForReal(): Int? {
        if (!isRunning) {
            isRunning = true
        }
        
        val totalPossible = currentRange.end - currentRange.start + 1
        
        if (!allowDuplicates && usedNumbers.size >= totalPossible) {
            return null // 所有可能的数字都已抽取完
        }
        
        if (allowDuplicates) {
            return Random.nextInt(currentRange.start, currentRange.end + 1)
        } else {
            // 不允许重复的情况下，寻找一个未使用的数字
            var attempts = 0
            val maxAttempts = totalPossible * 2 // 防止无限循环
            
            while (attempts < maxAttempts) {
                val num = Random.nextInt(currentRange.start, currentRange.end + 1)
                if (!usedNumbers.contains(num)) {
                    usedNumbers.add(num)
                    return num
                }
                attempts++
            }
            
            // 如果随机方法失败，尝试遍历查找
            for (num in currentRange.start..currentRange.end) {
                if (!usedNumbers.contains(num)) {
                    usedNumbers.add(num)
                    return num
                }
            }
            
            return null // 所有可能的数字都已抽取完
        }
    }
    
    // 用于动画的随机数生成，不计入实际抽取
    fun getRandomNumberForAnimation(): Int {
        return Random.nextInt(currentRange.start, currentRange.end + 1)
    }
    
    fun isAnimationRunning(): Boolean = isAnimating
    
    fun canContinue(): Boolean {
        if (!isRunning) return true
        
        if (allowDuplicates) return true
        
        val totalPossible = currentRange.end - currentRange.start + 1
        return usedNumbers.size < totalPossible
    }
    
    // 进度计算 - 仅在不允许重复时显示进度条，每次成功抽取数字进度+1
    fun getProgress(): Float {
        if (!isRunning) return 0f

        if (allowDuplicates) {
            // 如果允许重复，则不显示有意义的进度
            return 0f
        }

        val totalPossible = currentRange.end - currentRange.start + 1
        val currentProgress = usedNumbers.size
        
        // 根据要求，进度条最小值是1，最大值是开始值到结束值的总数
        // 进度值从1到总数，所以需要将当前进度+1来计算比例
        return if (totalPossible > 0) {
            // 当前进度（最小值为1）除以最大可能值
            val progressValue = if (currentProgress > 0) currentProgress else 0
            progressValue.toFloat() / totalPossible
        } else 0f
    }
    
    // 计算每次抽取的固定百分比（开启重复抽取时使用）
    fun getFixedPercentage(): Float {
        val totalPossible = currentRange.end - currentRange.start + 1
        return if (totalPossible > 0) 1.0f / totalPossible else 0f
    }
    
    // 获取当前百分比（关闭重复抽取时使用，除数随已使用数字数量递减）
    fun getCurrentPercentage(): Float {
        if (allowDuplicates) {
            // 如果允许重复，返回固定百分比
            return getFixedPercentage()
        } else {
            // 如果不允许重复，计算剩余数量的百分比
            val totalPossible = currentRange.end - currentRange.start + 1
            val remaining = totalPossible - usedNumbers.size
            return if (remaining > 0) 1.0f / remaining else 0f
        }
    }
    
    // 获取格式化的百分比字符串（开启重复抽取时使用）
    fun getFormattedFixedPercentageString(): String {
        val percentage = getFixedPercentage()
        val percentageValue = percentage * 100
        // 保留5位小数并省略尾部的0
        var formatted = String.format("%.5f", percentageValue)
        // 移除尾部的0，但保留小数点前的数字
        formatted = formatted.replace("0+$".toRegex(), "")  // 移除小数点后的尾随0
        formatted = formatted.replace("\\.$".toRegex(), "") // 如果最后是小数点，也移除
        return "${formatted}%"
    }
    
    // 获取格式化的当前百分比字符串（关闭重复抽取时使用）
    fun getFormattedCurrentPercentageString(): String {
        val percentage = getCurrentPercentage()
        val percentageValue = percentage * 100
        // 保留5位小数并省略尾部的0
        var formatted = String.format("%.5f", percentageValue)
        // 移除尾部的0，但保留小数点前的数字
        formatted = formatted.replace("0+$".toRegex(), "")  // 移除小数点后的尾随0
        formatted = formatted.replace("\\.$".toRegex(), "") // 如果最后是小数点，也移除
        return "${formatted}%"
    }
    
    fun getUsedNumbersCount(): Int = usedNumbers.size
    
    fun getTotalPossibleNumbers(): Int = if (isRunning) currentRange.end - currentRange.start + 1 else 0
}