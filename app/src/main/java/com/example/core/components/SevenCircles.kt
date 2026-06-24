package com.example.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SevenCircles(
    modifier: Modifier = Modifier,
    activeStates: List<String> = listOf("Red", "Yellow", "Blue", "Green", "Blue", "Yellow", "Red")
) {
    // Colors mapped to rating types
    val colorMap = mapOf(
        "Green" to Color(0xFF00E676), // Easy
        "Blue" to Color(0xFF2979FF),  // Good
        "Yellow" to Color(0xFFFFD600),// Hard
        "Red" to Color(0xFFFF1744)    // Again
    )

    val labelMap = mapOf(
        "Green" to "Easy",
        "Blue" to "Good",
        "Yellow" to "Hard",
        "Red" to "Again"
    )

    var selectedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            activeStates.forEachIndexed { index, stateName ->
                val targetColor = colorMap[stateName] ?: Color.Gray
                val isSelected = selectedIndex == index
                
                // Pulsing animation for selected or highlighted circle
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "circle_scale"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scale)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 2.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = targetColor,
                            spotColor = targetColor
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(targetColor, targetColor.copy(alpha = 0.6f))
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            selectedIndex = if (isSelected) -1 else index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 12.sp,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tooltip feedback
        if (selectedIndex != -1) {
            val stateName = activeStates[selectedIndex]
            val label = labelMap[stateName] ?: "Unknown"
            val stateColor = colorMap[stateName] ?: Color.Gray
            
            GlassCard(
                cornerRadius = 12.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tick ${selectedIndex + 1}: $label",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Text(
                text = "Tap a circle to view rating status",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
