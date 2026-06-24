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
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.TikiPlaceholder
import com.example.core.fsrs.CefrLevelMastery
import com.example.core.fsrs.LearningQualityScore
import java.util.Date
import kotlin.math.max

@Composable
fun AnalysisScreen() {
    val scrollState = rememberScrollState()
    val repo = remember { SevenTicksApplication.instance.userRepository }

    // Collect data state
    val allCards by repo.allCards.collectAsState(initial = emptyList())
    val reviewHistory by repo.reviewHistory.collectAsState(initial = emptyList())
    val userProgress by repo.userProgress.collectAsState(initial = null)

    var qualityScore by remember { mutableStateOf<LearningQualityScore?>(null) }
    var masteryList by remember { mutableStateOf<List<CefrLevelMastery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(allCards, reviewHistory) {
        isLoading = true
        qualityScore = repo.getLearningQualityScore()
        masteryList = repo.getCefrLevelMasteryList()
        isLoading = false
    }

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
            text = "Adaptive Analytics",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00C2FF))
            }
        } else {
            // 1. Progress Donut Card - Real Data
            val accuracyVal = qualityScore?.reviewAccuracy ?: 0.0f
            val accuracyPct = (accuracyVal * 100).toInt()
            
            val accuracyDescription = when {
                accuracyVal >= 0.90f -> "Outstanding! Your FSRS memory retention is in the optimal range."
                accuracyVal >= 0.80f -> "Great progress! Your retention exceeds the average learning velocity."
                accuracyVal >= 0.60f -> "Stable. Keep reviewing daily to build stronger neural pathways."
                else -> "FSRS is calibrating. Maintain a steady streak to improve retention."
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "Memory Retention",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = accuracyDescription,
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
                                style = Stroke(width = 10.dp.toPx())
                            )
                            // Active Arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF), Color(0xFF00C2FF))
                                ),
                                startAngle = -90f,
                                sweepAngle = (accuracyVal * 360f).coerceIn(10f, 360f),
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$accuracyPct%",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "FSRS Target",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // 2. Metrics Statistics Grid (Three cards row) - Real Data
            val newWordsCount = allCards.count { it.reps == 0 }
            val totalReviewed = reviewHistory.size
            val masteredCount = allCards.count { it.reps > 0 && it.stability >= 2.0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    MetricItem("New Queue", newWordsCount.toString(), Color(0xFF00C2FF)),
                    MetricItem("Total Reviews", totalReviewed.toString(), Color(0xFF9D00FF)),
                    MetricItem("FSRS Solid", masteredCount.toString(), Color(0xFF00E676))
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

            // 3. Spaced Repetition Level Mastery Section
            Text(
                text = "CEFR Level Mastery",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                masteryList.forEach { mastery ->
                    val progressPercent = (mastery.masteryPercentage * 100).toInt()
                    val isUnlocked = mastery.isUnlocked

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (isUnlocked) Color(0xFF00FFD2).copy(alpha = 0.15f)
                                                else Color.White.copy(alpha = 0.05f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mastery.level,
                                            color = if (isUnlocked) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "CEFR Level ${mastery.level}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Learned: ${mastery.wordsLearned} / Retained: ${mastery.wordsRetained}",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isUnlocked) {
                                        Icon(
                                            Icons.Default.LockOpen,
                                            contentDescription = "Unlocked",
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$progressPercent% Mastered",
                                            color = Color(0xFF00E676),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Locked",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Mastery Progress bar
                            if (isUnlocked) {
                                LinearProgressIndicator(
                                    progress = { mastery.masteryPercentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF00FFD2),
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Gorgeous Custom Vector Line Chart - Real Weekly Activity
            val now = System.currentTimeMillis()
            val weeklyCounts = remember(reviewHistory) {
                val counts = FloatArray(7)
                for (log in reviewHistory) {
                    val diffMs = now - log.timestamp
                    val diffDays = (diffMs / (24L * 60 * 60 * 1000)).toInt()
                    if (diffDays in 0..6) {
                        counts[6 - diffDays] += 1.0f
                    }
                }
                counts
            }

            val maxWeeklyCount = remember(weeklyCounts) {
                max(weeklyCounts.maxOrNull() ?: 1.0f, 5.0f)
            }

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

                        // Generate points based on weekly reviews
                        val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        for (i in 0..6) {
                            val ratio = weeklyCounts[i] / maxWeeklyCount
                            // h * 0.85f is floor, h * 0.15f is roof
                            val y = h * 0.85f - (ratio * h * 0.70f)
                            val x = w * 0.05f + (i.toFloat() / 6.0f) * w * 0.90f
                            points.add(androidx.compose.ui.geometry.Offset(x, y))
                        }

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

            // 5. Heatmap Card - Real consistency mapping
            val monthlyCounts = remember(reviewHistory) {
                val counts = IntArray(28)
                for (log in reviewHistory) {
                    val diffMs = now - log.timestamp
                    val diffDays = (diffMs / (24L * 60 * 60 * 1000)).toInt()
                    if (diffDays in 0..27) {
                        counts[27 - diffDays]++
                    }
                }
                counts
            }

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
                                    val index = row * 7 + col
                                    val count = monthlyCounts[index]
                                    
                                    val intensity = when {
                                        count == 0 -> 0.08f
                                        count <= 2 -> 0.35f
                                        count <= 5 -> 0.65f
                                        count <= 10 -> 0.85f
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
                        listOf(0.08f, 0.4f, 0.7f, 1.0f).forEach { intensity ->
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

            // 6. Interactive Tiki Mascot summary
            val currentLevel = userProgress?.level ?: 1
            val levelString = when (currentLevel) {
                1 -> "A1"
                2 -> "A2"
                3 -> "B1"
                4 -> "B2"
                5 -> "C1"
                6 -> "C2"
                else -> "A1"
            }
            
            val activeMastery = masteryList.find { it.level.equals(levelString, ignoreCase = true) }
            val wordsLearnedLevel = activeMastery?.wordsLearned ?: 0
            val totalInLevel = activeMastery?.totalWordsInLevel ?: 150
            
            val tikiMessage = when {
                totalReviewed == 0 -> "Welcome! Initiate your first adaptive study session to start analyzing metrics."
                accuracyVal >= 0.90f -> "Outstanding! Your retention score of $accuracyPct% is exceptional. FSRS stability metrics are solid."
                accuracyVal >= 0.75f -> "Excellent momentum! You are progressing steadily in level $levelString. Let's practice tomorrow."
                else -> "Great start! Reviewing consistently will optimize the FSRS interval schedule."
            }

            TikiPlaceholder(
                message = tikiMessage,
                sizeDp = 60,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class MetricItem(
    val title: String,
    val value: String,
    val color: Color
)
