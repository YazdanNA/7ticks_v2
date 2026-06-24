package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Glass surface background brush
    val glassBgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x1F7A88FF), // Semi-transparent blue
            Color(0x12D0BCFF), // Semi-transparent purple
            Color(0x0F000000)  // Dark backing
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    // Glass reflective border brush
    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x40FFFFFF), // Bright white highlight
            Color(0x1AFFFFFF), // Muted white
            Color(0x337A88FF), // Blue tint reflection
            Color(0x33D0BCFF)  // Purple tint reflection
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    val shape = RoundedCornerShape(cornerRadius)

    val baseModifier = modifier
        .clip(shape)
        .background(brush = glassBgBrush, shape = shape)
        .border(width = borderWidth, brush = glassBorderBrush, shape = shape)

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Box(
        modifier = clickableModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun PremiumGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    isSecondary: Boolean = false,
    enabled: Boolean = true
) {
    val gradientBrush = if (!enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x11FFFFFF),
                Color(0x11FFFFFF)
            )
        )
    } else if (!isSecondary) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00C2FF), // Vibrant blue
                Color(0xFF9D00FF)  // Vibrant purple
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x337A88FF),
                Color(0x33D0BCFF)
            )
        )
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = gradientBrush)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (enabled) listOf(Color(0x60FFFFFF), Color(0x10FFFFFF)) else listOf(Color(0x10FFFFFF), Color(0x05FFFFFF))
                ),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.graphicsLayer(alpha = if (enabled) 1f else 0.4f)
        ) {
            if (icon != null) {
                icon()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
            androidx.compose.material3.Text(
                text = text,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
    }
}
