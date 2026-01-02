package com.timome.sjxh.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DraggableSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val switchWidth = 50.dp
    val switchHeight = 30.dp
    val thumbSize = 26.dp
    
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) {
            with(LocalDensity.current) { (switchWidth - thumbSize).toPx() }
        } else 0f,
        label = "thumbPosition"
    )
    
    Box(
        modifier = modifier
            .width(switchWidth)
            .height(switchHeight)
            .clip(CircleShape)
            .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = with(LocalDensity.current) { thumbPosition.toDp() })
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}