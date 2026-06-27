package com.example.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SevenCircles(
    modifier: Modifier = Modifier,
    activeStates: List<String> = listOf("Gray", "Gray", "Gray", "Gray", "Gray", "Gray", "Gray")
) {
    // Standard Leitner & FSRS colors
    val colorMap = mapOf(
        "Green" to Color(0xFF00E676),  // Easy
        "Blue" to Color(0xFF2979FF),   // Good
        "Yellow" to Color(0xFFFFD600), // Hard
        "Red" to Color(0xFFFF1744),    // Again
        "Gray" to Color(0x33FFFFFF)    // Inactive empty
    )

    // Guarantee exactly 7 circles
    val states = activeStates.take(7).plus(List((7 - activeStates.size).coerceAtLeast(0)) { "Gray" })

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        states.forEachIndexed { index, stateName ->
            val targetColor = colorMap[stateName] ?: Color(0x33FFFFFF)
            val isActive = stateName != "Gray"

            // Spring scale animation when active
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.1f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "circle_scale"
            )

            // Smooth border color fade
            val borderColor = if (isActive) targetColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .scale(scale)
                    .shadow(
                        elevation = if (isActive) 4.dp else 0.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = targetColor,
                        spotColor = targetColor
                    )
                    .background(
                        brush = if (isActive) {
                            Brush.radialGradient(
                                colors = listOf(targetColor, targetColor.copy(alpha = 0.7f))
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF))
                            )
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    // Render symbols based on rating level
                    val symbol = when (stateName) {
                        "Green", "Blue" -> "✔"
                        "Yellow" -> "!"
                        "Red" -> "✘"
                        else -> ""
                    }
                    Text(
                        text = symbol,
                        color = if (stateName == "Yellow") Color(0xFF0F1026) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Render numeric position placeholder for unreviewed stages
                    Text(
                        text = "${index + 1}",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
