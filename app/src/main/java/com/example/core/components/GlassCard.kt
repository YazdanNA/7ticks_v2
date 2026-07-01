package com.example.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.isDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    glowColor: Color? = null,
    backgroundColor: Color? = null,
    depth: Int = 1,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isDark = MaterialTheme.colorScheme.isDark

    // Bouncy press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_card_scale"
    )

    // Glass surface background brush
    val glassBgBrush = if (backgroundColor != null) {
        Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = (0.16f * depth).coerceAtMost(0.45f)),
                backgroundColor.copy(alpha = (0.04f * depth).coerceAtMost(0.18f))
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x1F7A88FF), // Semi-transparent blue
                    Color(0x12D0BCFF), // Semi-transparent purple
                    Color(0x0F000000)  // Dark backing
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x0C4F46E5), // Light semi-transparent indigo
                    Color(0x0606B6D4), // Light semi-transparent cyan
                    Color(0x03FFFFFF)  // Light backing
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        }
    }

    // Glass reflective border brush
    val glassBorderBrush = if (glowColor != null) {
        Brush.linearGradient(
            colors = listOf(
                (if (isDark) Color.White else Color(0xFF0F172A)).copy(alpha = 0.25f),
                glowColor.copy(alpha = 0.12f),
                (if (isDark) Color.White else Color(0xFF0F172A)).copy(alpha = 0.04f)
            ),
            start = Offset(0f, 0f),
            end = Offset(200f, 400f)
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x40FFFFFF), // Bright white highlight
                    Color(0x1AFFFFFF), // Muted white
                    Color(0x337A88FF), // Blue tint reflection
                    Color(0x33D0BCFF)  // Purple tint reflection
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x200F172A), // Dark highlight
                    Color(0x0F0F172A), // Muted dark
                    Color(0x154F46E5), // Indigo tint reflection
                    Color(0x1506B6D4)  // Cyan tint reflection
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        }
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = (4 * depth).dp,
                shape = shape,
                clip = false,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color(0xFF0F172A).copy(alpha = 0.1f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0xFF0F172A).copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(brush = glassBgBrush, shape = shape)
            .border(width = borderWidth, brush = glassBorderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf((if (isDark) Color.White else Color(0xFF0F172A)).copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width / 3f, 0f),
                            radius = size.width / 1.5f
                        )
                    )
                }
                .padding(contentPadding)
        ) {
            content()
        }
    }
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
    val isDark = MaterialTheme.colorScheme.isDark
    val gradientBrush = if (!enabled) {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(Color(0x11FFFFFF), Color(0x11FFFFFF))
            } else {
                listOf(Color(0x0C000000), Color(0x0C000000))
            }
        )
    } else if (!isSecondary) {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
            } else {
                listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
            }
        )
    } else {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(Color(0x337A88FF), Color(0x33D0BCFF))
            } else {
                listOf(Color(0x1A6366F1), Color(0x0C4F46E5))
            }
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
                    colors = if (enabled) {
                        if (isDark) listOf(Color(0x60FFFFFF), Color(0x10FFFFFF)) else listOf(Color(0x330F172A), Color(0x0A0F172A))
                    } else {
                        if (isDark) listOf(Color(0x10FFFFFF), Color(0x05FFFFFF)) else listOf(Color(0x0F0F172A), Color(0x030F172A))
                    }
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
                color = if (!enabled) {
                    if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF0F172A).copy(alpha = 0.4f)
                } else if (!isSecondary) {
                    Color.White
                } else {
                    if (isDark) Color(0xFF00FFD2) else Color(0xFF6366F1)
                },
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
    }
}
