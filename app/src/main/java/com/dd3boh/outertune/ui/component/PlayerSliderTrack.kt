/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PlayerSliderTrack(
    sliderState: Any,
    modifier: Modifier = Modifier,
    colors: Any = Unit,
    trackHeight: Dp = 10.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    
    // Extract progress value from sliderState
    val progress = try {
        val field = sliderState.javaClass.getDeclaredField("value")
        field.isAccessible = true
        val value = field.get(sliderState) as Float
        
        val rangeField = sliderState.javaClass.getDeclaredField("valueRange")
        rangeField.isAccessible = true
        val range = rangeField.get(sliderState) as ClosedFloatingPointRange<*>
        
        (value - range.start as Float) / (range.endInclusive as Float - range.start as Float)
    } catch (e: Exception) {
        0f
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
    ) {
        drawTrack(
            progress = progress,
            activeTrackColor = primaryColor,
            inactiveTrackColor = onSurfaceColor,
            trackHeight = trackHeight
        )
    }
}

private fun DrawScope.drawTrack(
    progress: Float,
    activeTrackColor: Color,
    inactiveTrackColor: Color,
    trackHeight: Dp = 2.dp
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val trackStrokeWidth = trackHeight.toPx()
    
    // Draw inactive track
    drawLine(
        inactiveTrackColor,
        sliderStart,
        sliderEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
    
    // Draw active track
    val sliderValueEnd = Offset(
        sliderStart.x + (sliderEnd.x - sliderStart.x) * progress,
        center.y
    )
    
    drawLine(
        activeTrackColor,
        sliderStart,
        sliderValueEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
}