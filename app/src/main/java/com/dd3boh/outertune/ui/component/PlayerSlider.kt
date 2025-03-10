/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Custom slider for media playback that can display either a wavy style seekbar or a regular slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSlider(
    modifier: Modifier = Modifier,
    progress: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onProgressChange: (Float) -> Unit,
    onProgressFinish: (() -> Unit)? = null,
    useWavyStyle: Boolean = true,
    isAnimated: Boolean = false,
    waveAmplitude: Float = 1.0f
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    if (useWavyStyle) {
        WavySeekbar(
            modifier = modifier,
            progress = progress,
            valueRange = valueRange,
            onProgressChange = onProgressChange,
            onProgressFinish = onProgressFinish,
            isAnimated = isAnimated,
            waveAmplitude = waveAmplitude
        )
    } else {
        // Simple custom slider without Material3
        CustomSimpleSlider(
            modifier = modifier,
            progress = progress,
            valueRange = valueRange,
            onProgressChange = onProgressChange,
            onProgressFinish = onProgressFinish
        )
    }
}

@Composable
private fun CustomSimpleSlider(
    modifier: Modifier = Modifier,
    progress: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onProgressChange: (Float) -> Unit,
    onProgressFinish: (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    
    var width by remember { mutableStateOf(0f) }
    
    // Convert absolute progress to relative progress (0f to 1f)
    val relativeProgress = if (valueRange.endInclusive - valueRange.start > 0f) {
        (progress - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    } else {
        0f
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .onSizeChanged { 
                width = it.width.toFloat() 
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val pixelsToDragPercent = delta / width
                    val newRelativeValue = (relativeProgress + pixelsToDragPercent).coerceIn(0f, 1f)
                    val newAbsoluteValue = valueRange.start + newRelativeValue * (valueRange.endInclusive - valueRange.start)
                    onProgressChange(newAbsoluteValue)
                },
                onDragStopped = { onProgressFinish?.invoke() }
            )
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val progressWidth = canvasWidth * relativeProgress
            val trackHeight = 4.dp.toPx()
            val centerY = canvasHeight / 2
            val thumbRadius = 8.dp.toPx()
            
            // Draw inactive track
            drawLine(
                color = onSurfaceColor,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )
            
            // Draw active track
            if (progressWidth > 0f) {
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, centerY),
                    end = Offset(progressWidth, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
            }
            
            // Draw thumb
            if (progressWidth >= 0f) {
                drawCircle(
                    color = primaryColor,
                    radius = thumbRadius,
                    center = Offset(progressWidth, centerY)
                )
            }
        }
    }
}

@Composable
private fun WavySeekbar(
    modifier: Modifier = Modifier,
    progress: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onProgressChange: (Float) -> Unit,
    onProgressFinish: (() -> Unit)? = null,
    isAnimated: Boolean = false,
    waveAmplitude: Float = 1.0f
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    
    var phase by remember { mutableFloatStateOf(0f) }
    
    // Animation effect for wave movement
    LaunchedEffect(isAnimated) {
        while (isAnimated) {
            phase += 0.05f
            if (phase > 2 * Math.PI) {
                phase = 0f
            }
            delay(16) // ~60fps animation
        }
    }
    
    // Convert absolute progress to relative progress (0f to 1f)
    val relativeProgress = if (valueRange.endInclusive - valueRange.start > 0f) {
        (progress - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    } else {
        0f
    }
    
    // Use a Box to stack the invisible slider for interaction on top of our custom drawing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Invisible slider to handle interactions
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = progress,
            valueRange = valueRange,
            onValueChange = onProgressChange,
            onValueChangeFinished = onProgressFinish,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
        
        // Our custom wavy visualization
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            drawSmoothWave(
                progress = relativeProgress,
                activeColor = primaryColor,
                inactiveColor = onSurfaceColor,
                phase = phase,
                amplitude = waveAmplitude
            )
        }
    }
}

/**
 * Draws a smooth wavy line using Path instead of line segments
 */
private fun DrawScope.drawSmoothWave(
    progress: Float,
    activeColor: Color,
    inactiveColor: Color,
    phase: Float,
    amplitude: Float
) {
    val width = size.width
    val height = size.height
    val progressPoint = width * progress
    val centerY = height / 2
    
    // Wave parameters
    val waveAmplitude = height / 4 * amplitude
    val waveLength = 30.dp.toPx() // Wavelength
    val lineThickness = 2.dp.toPx()
    
    // Create paths for active and inactive portions
    val activePath = Path()
    val inactivePath = Path()
    
    // Initialize paths
    activePath.moveTo(0f, centerY)
    inactivePath.moveTo(progressPoint, centerY)
    
    // Step size for smooth curve (smaller = smoother)
    val step = 2f
    var x = step
    
    // Draw active portion
    while (x <= progressPoint) {
        val y = centerY + waveAmplitude * sin((x / waveLength) * 2 * Math.PI + phase).toFloat()
        activePath.lineTo(x, y)
        x += step
    }
    
    // Draw inactive portion
    x = progressPoint + step
    while (x <= width) {
        val y = centerY + waveAmplitude * sin((x / waveLength) * 2 * Math.PI + phase).toFloat()
        inactivePath.lineTo(x, y)
        x += step
    }
    
    // Draw the paths
    drawPath(
        path = activePath,
        color = activeColor,
        style = Stroke(width = lineThickness, cap = StrokeCap.Round)
    )
    
    drawPath(
        path = inactivePath,
        color = inactiveColor,
        style = Stroke(width = lineThickness, cap = StrokeCap.Round)
    )
    
    // Draw thumb at progress point if needed
    if (progress > 0) {
        val thumbY = centerY + waveAmplitude * sin((progressPoint / waveLength) * 2 * Math.PI + phase).toFloat()
        
        // Draw outer glow/shadow
        drawCircle(
            color = activeColor.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = Offset(progressPoint, thumbY)
        )
        
        // Draw main thumb
        drawCircle(
            color = activeColor,
            radius = 5.dp.toPx(),
            center = Offset(progressPoint, thumbY)
        )
    }
}
