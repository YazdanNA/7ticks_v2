package com.example.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TikiPlaceholder(
    modifier: Modifier = Modifier,
    sizeDp: Int = 80,
    message: String = "Tiki is listening..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tiki")
    
    // Smooth hovering animation
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiki_hover"
    )

    // Glowing radius pulsation
    val glowRadiusPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiki_glow"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .offset(y = hoverY.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient glow behind Tiki
            Canvas(modifier = Modifier.fillMaxSize().scale(glowRadiusPulse)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3300FFD2), Color.Transparent),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f
                )
            }

            // Tiki Glass Container Bubble
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.85).dp)
                    .background(Color(0x22FFFFFF), CircleShape)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x80FFFFFF), Color(0x1000FFD2))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Vector Tiki Face drawing inside bubble
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val w = size.width
                    val h = size.height

                    // Styled Tiki wooden block face
                    drawRoundRect(
                        color = Color(0xFF9D00FF),
                        topLeft = Offset(w * 0.25f, h * 0.2f),
                        size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )

                    // Glow edge for face
                    drawRoundRect(
                        color = Color(0xFF00C2FF),
                        topLeft = Offset(w * 0.25f, h * 0.2f),
                        size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 2f)
                    )

                    // Big glowing Tiki eyes
                    drawCircle(
                        color = Color(0xFF00FFD2),
                        center = Offset(w * 0.38f, h * 0.42f),
                        radius = w * 0.08f
                    )
                    drawCircle(
                        color = Color(0xFF00FFD2),
                        center = Offset(w * 0.62f, h * 0.42f),
                        radius = w * 0.08f
                    )

                    // Inner pupils
                    drawCircle(
                        color = Color(0xFF0F1026),
                        center = Offset(w * 0.38f, h * 0.42f),
                        radius = w * 0.04f
                    )
                    drawCircle(
                        color = Color(0xFF0F1026),
                        center = Offset(w * 0.62f, h * 0.42f),
                        radius = w * 0.04f
                    )

                    // Tribal pattern (Cheeks)
                    drawLine(
                        color = Color(0xFFFFD600),
                        start = Offset(w * 0.32f, h * 0.58f),
                        end = Offset(w * 0.38f, h * 0.58f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFFFFD600),
                        start = Offset(w * 0.68f, h * 0.58f),
                        end = Offset(w * 0.62f, h * 0.58f),
                        strokeWidth = 3f
                    )

                    // Happy curve mouth
                    drawArc(
                        color = Color(0xFFFFD600),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.42f, h * 0.52f),
                        size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.12f),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(
                cornerRadius = 12.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = message,
                    color = Color(0xFF00FFD2),
                    fontSize = 11.sp,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
