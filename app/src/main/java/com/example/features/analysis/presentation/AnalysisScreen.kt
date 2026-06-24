package com.example.features.analysis.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.GlassCard
import com.example.core.components.TikiPlaceholder

@Composable
fun AnalysisScreen() {
    val scrollState = rememberScrollState()
    
    // Animation trigger for charts
    val infiniteTransition = rememberInfiniteTransition(label = "analysis")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Learning Analysis",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // 1. Progress Donut Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "Overall Progress",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You are performing better than 94% of users at level 7.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Vector Progress Donut
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background circle
                        drawCircle(
                            color = Color(0x1100C2FF),
                            style = Stroke(width = 12.dp.toPx())
                        )
                        // Active Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF), Color(0xFF00C2FF))
                            ),
                            startAngle = -90f,
                            sweepAngle = 280f, // 78% of 360
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "78%",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Accuracy",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // 2. Metrics Statistics Grid (Three cards row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                MetricItem("New Words", "126", Color(0xFF00C2FF)),
                MetricItem("Reviewed", "342", Color(0xFF9D00FF)),
                MetricItem("Mastered", "89", Color(0xFF00E676))
            ).forEach { item ->
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(item.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = item.color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.value,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = item.title,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Gorgeous Custom Vector Line Chart
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column {
                Text(
                    text = "Weekly Activity Profile",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vector Canvas drawing representing progress weekly line chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw grid reference horizontal lines
                    drawLine(Color(0x0FFFFFFF), start = androidx.compose.ui.geometry.Offset(0f, h * 0.2f), end = androidx.compose.ui.geometry.Offset(w, h * 0.2f), strokeWidth = 2f)
                    drawLine(Color(0x0FFFFFFF), start = androidx.compose.ui.geometry.Offset(0f, h * 0.5f), end = androidx.compose.ui.geometry.Offset(w, h * 0.5f), strokeWidth = 2f)
                    drawLine(Color(0x0FFFFFFF), start = androidx.compose.ui.geometry.Offset(0f, h * 0.8f), end = androidx.compose.ui.geometry.Offset(w, h * 0.8f), strokeWidth = 2f)

                    // Draw line path representing activity
                    val points = listOf(
                        androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.85f),
                        androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.55f),
                        androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.70f),
                        androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.30f),
                        androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.45f),
                        androidx.compose.ui.geometry.Offset(w * 0.80f, h * 0.15f),
                        androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.25f)
                    )

                    val path = Path()
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }

                    // Draw glowing gradient line path
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                        ),
                        style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    // Draw dots on path
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF00FFD2),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
                
                // Days Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(
                            text = day,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 4. Heatmap Placeholder (4 weeks x 7 days grid)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column {
                Text(
                    text = "Spaced Repetition Consistency",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your daily review consistency mapping on FSRS algorithm:",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Heatmap Grid: 4 rows x 7 columns
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0 until 7) {
                                // Dynamic alpha to simulate GitHub activity heatmap
                                val intensity = when ((row * 7 + col) % 5) {
                                    0 -> 0.1f
                                    1 -> 0.35f
                                    2 -> 0.6f
                                    3 -> 0.85f
                                    else -> 1.0f
                                }
                                val color = Color(0xFF00E676).copy(alpha = intensity)
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less  ", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    listOf(0.1f, 0.4f, 0.7f, 1.0f).forEach { intensity ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF00E676).copy(alpha = intensity))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("  More", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }

        // 5. Tiki Mascot slot
        TikiPlaceholder(
            message = "Your learning velocity increased by 18% this week! Let's keep the momentum going tomorrow.",
            sizeDp = 60,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class MetricItem(
    val title: String,
    val value: String,
    val color: Color
)
