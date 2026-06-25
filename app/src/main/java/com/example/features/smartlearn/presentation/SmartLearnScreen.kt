package com.example.features.smartlearn.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.navigation.Screen
import com.example.core.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartLearnScreen(navController: NavController) {
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val allCards by repo.allCards.collectAsState(initial = emptyList())
    val sessionState by repo.sessionState.collectAsState(initial = null)
    val statisticsList by repo.statistics.collectAsState(initial = emptyList())
    val challengesList by repo.challenges.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()

    val userName = userProgress?.userName ?: prefs.userName
    val streak = userProgress?.streak ?: 1
    val level = userProgress?.level ?: 1
    val xp = userProgress?.xp ?: 0
    val targetLg = userProgress?.targetLanguage ?: prefs.targetLanguage

    val totalWords = allCards.size
    val masteredWords = allCards.count { it.boxIndex >= 7 }
    val progressPct = if (totalWords > 0) (masteredWords * 100) / totalWords else 0

    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayStats = statisticsList.find { it.dateStr == todayDateStr }

    val dailyGoal = userProgress?.dailyGoal ?: prefs.dailyGoal
    val goalLower = dailyGoal.lowercase()
    val dailyNewLimit = when {
        goalLower.contains("5 min") || goalLower.contains("5-min") -> 2
        goalLower.contains("10 min") -> 3
        goalLower.contains("15 min") -> 4
        goalLower.contains("20 min") -> 5
        goalLower.contains("30 min") -> 8
        goalLower.contains("45 min") -> 12
        goalLower.contains("60 min") -> 15
        else -> 8
    }
    val dailyReviewLimit = when {
        goalLower.contains("5 min") || goalLower.contains("5-min") -> 8
        goalLower.contains("10 min") -> 10
        goalLower.contains("15 min") -> 15
        goalLower.contains("20 min") -> 20
        goalLower.contains("30 min") -> 30
        goalLower.contains("45 min") -> 45
        goalLower.contains("60 min") -> 60
        else -> 30
    }
    val learnedToday = todayStats?.wordsLearned ?: 0
    val reviewedToday = todayStats?.wordsReviewed ?: 0

    val remainingReviewsCapacity = (dailyReviewLimit - reviewedToday).coerceAtLeast(0)
    val remainingNewWordsCapacity = (dailyNewLimit - learnedToday).coerceAtLeast(0)

    val hasDueReviews = allCards.any { it.reps > 0 && it.dueDate <= System.currentTimeMillis() && it.state == 2 }
    val hasLearningCards = allCards.any { it.reps > 0 && it.state == 1 }
    val hasRelearningCards = allCards.any { it.reps > 0 && it.state == 3 }
    val isBudgetExhausted = learnedToday >= dailyNewLimit

    val overdueReviewsCount = allCards.count { it.reps > 0 && it.dueDate <= System.currentTimeMillis() && it.state == 2 }
    val learningCardsCount = allCards.count { it.reps > 0 && it.state == 1 }
    val relearningCardsCount = allCards.count { it.reps > 0 && it.state == 3 }
    val totalReviewWorkload = overdueReviewsCount + learningCardsCount + relearningCardsCount

    val dueCount: Int
    val learningCount: Int
    val newCount: Int

    if (totalReviewWorkload > 0) {
        dueCount = overdueReviewsCount.coerceAtMost(remainingReviewsCapacity)
        learningCount = (learningCardsCount + relearningCardsCount).coerceAtMost((remainingReviewsCapacity - dueCount).coerceAtLeast(0))
        newCount = 0
    } else {
        dueCount = 0
        learningCount = 0
        newCount = remainingNewWordsCapacity
    }

    val isFinishedForToday = (!hasDueReviews && !hasLearningCards && !hasRelearningCards && isBudgetExhausted) || (dueCount == 0 && learningCount == 0 && newCount == 0)

    val isSessionActive = sessionState?.active == true && !isFinishedForToday

    // Portal expansion state
    var showChallengePortal by remember { mutableStateOf(false) }

    // Breathing Hero Button Transition parameters
    val infiniteTransition = rememberInfiniteTransition(label = "hero_button_pulse")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breathe"
    )

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    val glowGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    // Animated counters for premium level up and XP increases
    val animStreak by animateIntAsState(targetValue = streak, animationSpec = tween(1200, easing = EaseOutQuad), label = "anim_streak")
    val animLevel by animateIntAsState(targetValue = level, animationSpec = tween(1000, easing = EaseOutQuad), label = "anim_level")
    val animXp by animateIntAsState(targetValue = xp, animationSpec = tween(1500, easing = EaseOutQuad), label = "anim_xp")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, $userName! 👋",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Today's Target: ${userProgress?.dailyGoal ?: prefs.dailyGoal}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x0CFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = Color(0xFF00FFD2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ORDER 1: XP + Level + Streak cards (Single row, responsive, animated counters)
            SharedProgressHeader(
                streak = animStreak,
                level = animLevel,
                xp = animXp,
                badgeText = "Cognitive Guru"
            )

            // ORDER 2: Ticky Card (Unified Ticky component)
            TickyCard(
                tikiState = "st-happy",
                sizeDp = 80,
                messages = listOf(
                    "Welcome back, $userName! 👋 Ready for another cognitive boost?",
                    if (isSessionActive) "You have an active spacing session waiting!" else "Your neural pathways are highly receptive right now!",
                    "Daily quests are active! Let's conquer some academic words!"
                )
            )

            // ORDER 3: Active Challenge Card (Expandable card with seamless morph animation)
            if (challengesList.isNotEmpty()) {
                val challenge = challengesList.firstOrNull { !it.completed } ?: challengesList.first()
                val progress = if (challenge.target > 0) challenge.current.toFloat() / challenge.target.toFloat() else 0f

                SharedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showChallengePortal = true
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (challenge.completed) Icons.Default.Check else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Challenge",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (challenge.completed) Color(0x3300E676) else Color(0x1F9D00FF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = challenge.title,
                                    color = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00FFD2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = challenge.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = if (challenge.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                trackColor = Color(0x1AFFFFFF),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "${challenge.current}/${challenge.target}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Tap to expand Quests portal",
                            color = Color(0xFF00C2FF).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // ORDER 4: English Workspace + Continue Learning Card (Merged into one intelligent dominant card)
            SharedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x159D00FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF9D00FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$targetLg Workspace",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$masteredWords of $totalWords mastered",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = "$progressPct%",
                            color = Color(0xFF00FFD2),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Stat Indicators Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dueCount",
                                color = Color(0xFFFF1744),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Reviews",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$learningCount",
                                color = Color(0xFF00C2FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Learning",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$newCount",
                                color = Color(0xFF00E676),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "New Words",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$masteredWords",
                                color = Color(0xFFFFD600),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Mastered",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x12FFFFFF))
                    )

                    // Continue Learning CTA (large, beautiful, pulsating, inside the same card!)
                    val btnInteractionSource = remember { MutableInteractionSource() }
                    val btnPressed by btnInteractionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(
                        targetValue = if (isFinishedForToday) 1.0f else if (btnPressed) 0.95f else breatheScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "btn_press_breathe"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(buttonScale)
                            .shadow(
                                elevation = if (isFinishedForToday) 4.dp else 16.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = false,
                                ambientColor = if (isFinishedForToday) Color.Transparent else Color(0xFF00C2FF).copy(alpha = glowGlow * 0.4f),
                                spotColor = if (isFinishedForToday) Color.Transparent else Color(0xFF9D00FF).copy(alpha = glowGlow * 0.5f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isFinishedForToday) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2C2D35), Color(0xFF25262B))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                                    )
                                }
                            )
                            .clickable(
                                enabled = !isFinishedForToday,
                                interactionSource = btnInteractionSource,
                                indication = if (isFinishedForToday) null else LocalIndication.current
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    if (!isSessionActive) {
                                        repo.generateSmartLearnSession()
                                    }
                                    navController.navigate(Screen.LearningSession.route)
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isFinishedForToday) Icons.Default.CheckCircle else if (isSessionActive) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isFinishedForToday) Color(0xFF00E676) else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isFinishedForToday) "YOU'RE DONE FOR TODAY" else if (isSessionActive) "CONTINUE LEARNING" else "START STUDY SESSION",
                                color = if (isFinishedForToday) Color(0xFF00E676) else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // SECTION 9: ACTIVE CHALLENGE CARD EXPANSION PORTAL (Seamless morph transition to fill the screen)
        val expansionFraction by animateFloatAsState(
            targetValue = if (showChallengePortal) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "challenge_expansion"
        )

        if (expansionFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF060713).copy(alpha = expansionFraction * 0.96f))
                    .clickable(enabled = expansionFraction == 1f) {
                        showChallengePortal = false
                    }
            ) {
                val scale = 0.92f + (0.08f * expansionFraction)
                val alpha = expansionFraction

                SharedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f + (0.08f * expansionFraction))
                        .fillMaxHeight(0.75f + (0.25f * expansionFraction))
                        .align(Alignment.Center)
                        .scale(scale)
                        .graphicsLayer { this.alpha = alpha },
                    cornerRadius = (24 * (1f - expansionFraction) + 28 * expansionFraction).dp,
                    backgroundColor = Color(0xFF0F1026).copy(alpha = 0.9f + 0.1f * expansionFraction)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Portal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD600),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Challenges",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showChallengePortal = false
                                },
                                modifier = Modifier
                                    .background(Color(0x12FFFFFF), CircleShape)
                                    .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Portal",
                                    tint = Color.White
                                )
                            }
                        }

                        // Scrollable challenge list
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val portalScrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(portalScrollState),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                challengesList.forEach { item ->
                                    val isDailyItem = item.id.startsWith("daily")
                                    val itemProgress = if (item.target > 0) item.current.toFloat() / item.target.toFloat() else 0f

                                    SharedGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp,
                                        backgroundColor = if (item.completed) Color(0x1A00E676) else Color(0x0CFFFFFF)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (item.completed) Icons.Default.Check else Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (item.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.title,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Text(
                                                    text = if (isDailyItem) "+150 XP" else "+250 XP",
                                                    color = if (item.completed) Color(0xFF00E676) else Color(0xFF00FFD2),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Text(
                                                text = item.description,
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                LinearProgressIndicator(
                                                    progress = { itemProgress },
                                                    color = if (item.completed) Color(0xFF00E676) else Color(0xFF00C2FF),
                                                    trackColor = Color(0x0CFFFFFF),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                )
                                                Text(
                                                    text = "${item.current}/${item.target}",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Portal Footer Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00FFD2),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quests synchronize in real-time with the central FSRS engine",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
