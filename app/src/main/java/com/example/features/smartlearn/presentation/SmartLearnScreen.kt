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
import com.example.core.localization.localize
import com.example.core.navigation.Screen
import com.example.core.ui.components.*
import com.example.ui.theme.isDark
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas

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

    // Retrieve goal minutes dynamically from the goal string
    val goalMinutes = remember(goalLower) {
        val regex = "(\\d+)".toRegex()
        val match = regex.find(goalLower)
        match?.value?.toIntOrNull() ?: 10
    }

    // Retrieve rolling average and calculate estimated dynamic capacity
    var rollingAverageSeconds by remember { mutableStateOf(20.0) }
    LaunchedEffect(repo) {
        rollingAverageSeconds = repo.smartSessionEngine.getStatsManager().getRollingAverageSeconds()
    }

    val estimatedCapacity = remember(goalMinutes, rollingAverageSeconds) {
        val studySeconds = goalMinutes * 60
        com.example.core.learning.engine.SessionCapacityCalculator().calculateCapacity(studySeconds, rollingAverageSeconds)
    }

    val learnedToday = todayStats?.wordsLearned ?: 0
    val reviewedToday = todayStats?.wordsReviewed ?: 0
    val totalStudiedToday = learnedToday + reviewedToday

    val remainingCapacity = (estimatedCapacity - totalStudiedToday).coerceAtLeast(0)

    val hasDueReviews = allCards.any { it.reps > 0 && it.dueDate <= System.currentTimeMillis() && it.state == 2 }
    val hasLearningCards = allCards.any { it.reps > 0 && it.state == 1 }
    val hasRelearningCards = allCards.any { it.reps > 0 && it.state == 3 }

    val overdueReviewsCount = allCards.count { it.reps > 0 && it.dueDate <= System.currentTimeMillis() && it.state == 2 }
    val learningCardsCount = allCards.count { it.reps > 0 && it.state == 1 }
    val relearningCardsCount = allCards.count { it.reps > 0 && it.state == 3 }
    val totalReviewWorkload = overdueReviewsCount + learningCardsCount + relearningCardsCount

    val dueCount: Int
    val learningCount: Int
    val newCount: Int

    if (totalReviewWorkload > 0) {
        dueCount = overdueReviewsCount.coerceAtMost(remainingCapacity)
        learningCount = (learningCardsCount + relearningCardsCount).coerceAtMost((remainingCapacity - dueCount).coerceAtLeast(0))
        newCount = (remainingCapacity - dueCount - learningCount).coerceAtLeast(0)
    } else {
        dueCount = 0
        learningCount = 0
        newCount = remainingCapacity
    }

    val isFinishedForToday = remainingCapacity <= 0 || (!hasDueReviews && !hasLearningCards && !hasRelearningCards && totalStudiedToday >= estimatedCapacity) || (dueCount == 0 && learningCount == 0 && newCount == 0)

    val isSessionActive = sessionState?.active == true && !isFinishedForToday

    val activeSessionCards = remember(sessionState, allCards) {
        if (sessionState != null && sessionState!!.active && sessionState!!.cardIds.isNotEmpty()) {
            val ids = sessionState!!.cardIds.split(",").filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }.toSet()
            allCards.filter { it.id in ids }
        } else {
            emptyList()
        }
    }

    val finalDueCount = if (isSessionActive && activeSessionCards.isNotEmpty()) activeSessionCards.count { it.state == 2 } else dueCount
    val finalLearningCount = if (isSessionActive && activeSessionCards.isNotEmpty()) activeSessionCards.count { it.state == 1 || it.state == 3 } else learningCount
    val finalNewCount = if (isSessionActive && activeSessionCards.isNotEmpty()) activeSessionCards.count { it.state == 0 } else newCount

    // Portal expansion state
    var showChallengePortal by remember { mutableStateOf(false) }
    var showStreakCelebration by remember { mutableStateOf(false) }
    var animatedStreakCount by remember { mutableIntStateOf(0) }

    androidx.activity.compose.BackHandler(enabled = showChallengePortal || showStreakCelebration) {
        if (showChallengePortal) showChallengePortal = false
        if (showStreakCelebration) showStreakCelebration = false
    }

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
        val isDark = MaterialTheme.colorScheme.isDark
        val textColor = if (isDark) Color.White else Color(0xFF0F172A)
        val subtextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header (FIXED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, $userName! 👋".localize(),
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Today's Target: ${userProgress?.dailyGoal ?: prefs.dailyGoal}".localize(),
                        color = subtextColor,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x0CFFFFFF) else Color(0x0C0F172A))
                        .border(1.dp, if (isDark) Color(0x1AFFFFFF) else Color(0x1A0F172A), CircleShape),
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

            // ORDER 1: XP + Level + Streak cards (Single row, responsive, animated counters - FIXED)
            SharedProgressHeader(
                streak = animStreak,
                level = animLevel,
                xp = animXp,
                badgeText = "Cognitive Guru",
                onStreakClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showStreakCelebration = true
                }
            )

            // Scrollable Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

            // ORDER 3: English Workspace + Continue Learning Card (Merged into one intelligent dominant card)
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
                                    text = "$targetLg Workspace".localize(),
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$masteredWords of $totalWords mastered".localize(),
                                    color = subtextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = "$progressPct%",
                            color = if (isDark) Color(0xFF00FFD2) else Color(0xFF0284C7),
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
                                text = "$finalDueCount",
                                color = Color(0xFFFF1744),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Reviews".localize(),
                                color = subtextColor,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$finalLearningCount",
                                color = Color(0xFF00C2FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Learning".localize(),
                                color = subtextColor,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$finalNewCount",
                                color = Color(0xFF00E676),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "New Words".localize(),
                                color = subtextColor,
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
                                text = "Mastered".localize(),
                                color = subtextColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color(0x12FFFFFF) else Color(0x1F0F172A))
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
                                elevation = if (isFinishedForToday) 0.dp else 12.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = false,
                                ambientColor = if (isFinishedForToday) Color.Transparent else (if (isDark) Color(0xFF00C2FF) else Color(0xFF6366F1)).copy(alpha = glowGlow * 0.4f),
                                spotColor = if (isFinishedForToday) Color.Transparent else (if (isDark) Color(0xFF9D00FF) else Color(0xFF4F46E5)).copy(alpha = glowGlow * 0.5f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isFinishedForToday) {
                                    Brush.horizontalGradient(
                                        colors = if (isDark) listOf(Color(0xFF2C2D35), Color(0xFF25262B)) else listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = if (isDark) listOf(Color(0xFF00C2FF), Color(0xFF9D00FF)) else listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
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
                                tint = if (isFinishedForToday) {
                                    if (isDark) Color(0xFF00E676) else Color(0xFF15803D)
                                } else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = (if (isFinishedForToday) "YOU'RE DONE FOR TODAY" else if (isSessionActive) "CONTINUE LEARNING" else "START STUDY SESSION").localize(),
                                color = if (isFinishedForToday) {
                                    if (isDark) Color(0xFF00E676) else Color(0xFF15803D)
                                } else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // ORDER 4: Active Challenge Card (Expandable card with seamless morph animation)
            if (challengesList.isNotEmpty()) {
                val challenge = challengesList.firstOrNull { !it.completed } ?: challengesList.first()
                val progress = if (challenge.target > 0) challenge.current.toFloat() / challenge.target.toFloat() else 0f

                SharedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(Screen.Challenges.route)
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
                                    text = "Active Challenge".localize(),
                                    color = textColor,
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
                                    text = challenge.title.localize(),
                                    color = if (challenge.completed) Color(0xFF00E676) else (if (isDark) Color(0xFF00FFD2) else Color(0xFF0284C7)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = challenge.description.localize(),
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155),
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
                                trackColor = if (isDark) Color(0x1AFFFFFF) else Color(0x1F0F172A),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "${challenge.current}/${challenge.target}",
                                color = subtextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Tap to view all challenges".localize(),
                            color = if (isDark) Color(0xFF00C2FF).copy(alpha = 0.7f) else Color(0xFF0284C7).copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(120.dp))
            }
        }


        // SECTION 9: ACTIVE CHALLENGE CARD EXPANSION PORTAL - DEACTIVATED (Navigates to separate screen)

        // SECTION 9.5: STREAK CELEBRATION OVERLAY (Duolingo-style grand gamified overlay with sounds and vibrations)
        if (showStreakCelebration) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val feedbackManager = remember { com.example.core.feedback.FeedbackManager.getInstance(context) }
            
            // Trigger sound & haptic progression once
            LaunchedEffect(showStreakCelebration) {
                animatedStreakCount = 0
                delay(300)
                val targetStreak = streak.coerceAtLeast(1)
                val stepDelay = (600 / targetStreak).coerceIn(40, 200).toLong()
                for (i in 1..targetStreak) {
                    delay(stepDelay)
                    animatedStreakCount = i
                    feedbackManager.playSound("typing")
                    feedbackManager.vibrateLight()
                }
                feedbackManager.playSound("streak")
                feedbackManager.vibrateHeavy()
            }

            val isDark = MaterialTheme.colorScheme.isDark
            val overlayBgColor = if (isDark) Color(0xE0060713) else Color(0xC00F172A)
            val celebrationCardColor = if (isDark) Color(0xFB0F1026) else Color.White

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayBgColor)
                    .clickable(enabled = true) {
                        // Dismiss on click background
                        showStreakCelebration = false
                    },
                contentAlignment = Alignment.Center
            ) {
                val flameScale = rememberInfiniteTransition(label = "flame_breathe").animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame_scale"
                )

                SharedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                        .clickable(enabled = false) {}, // prevent click-through
                    cornerRadius = 28.dp,
                    backgroundColor = celebrationCardColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "STREAK BURNING! 🔥".localize(),
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        // 3D/Glow Canvas Flame draw
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(flameScale.value),
                            contentAlignment = Alignment.Center
                        ) {
                            // Radial shadow back glow
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color(0x33FF3D00), Color.Transparent)
                                        )
                                    )
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.5f, h * 0.08f)
                                    // Left curve
                                    cubicTo(w * 0.1f, h * 0.45f, w * 0.05f, h * 0.85f, w * 0.5f, h * 0.98f)
                                    // Right curve
                                    cubicTo(w * 0.95f, h * 0.85f, w * 0.9f, h * 0.45f, w * 0.5f, h * 0.08f)
                                }
                                drawPath(
                                    path = path,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFEA00), // Yellow top
                                            Color(0xFFFF9100), // Orange mid
                                            Color(0xFFFF3D00)  // Red bottom
                                        )
                                    )
                                )
                                
                                // Inner flame
                                val innerPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.5f, h * 0.35f)
                                    cubicTo(w * 0.25f, h * 0.55f, w * 0.22f, h * 0.82f, w * 0.5f, h * 0.92f)
                                    cubicTo(w * 0.78f, h * 0.82f, w * 0.75f, h * 0.55f, w * 0.5f, h * 0.35f)
                                }
                                drawPath(
                                    path = innerPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF), // White core
                                            Color(0xFFFFEA00)  // Yellow border
                                        )
                                    )
                                )
                            }
                        }

                        // Big Counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$animatedStreakCount",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 48.sp,
                                modifier = Modifier.scale(if (animatedStreakCount == streak) 1.15f else 1.0f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Days".localize(),
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "استریک روزانه شما فعال است! از تداوم زنجیره یادگیری خود محافظت کنید.",
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF1E293B),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // 7-day calendar row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                            daysOfWeek.forEachIndexed { index, day ->
                                val active = index < (streak % 7).coerceAtLeast(1)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day,
                                        color = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (active) Brush.radialGradient(
                                                    colors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
                                                ) else Brush.radialGradient(
                                                    colors = listOf(
                                                        if (isDark) Color(0x12FFFFFF) else Color(0x0C0F172A),
                                                        if (isDark) Color(0x06FFFFFF) else Color(0x030F172A)
                                                    )
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                if (active) Color(0xFFFFEA00).copy(alpha = 0.6f) else (if (isDark) Color(0x12FFFFFF) else Color(0x120F172A)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (active) Color.White else (if (isDark) Color.White.copy(alpha = 0.15f) else Color(0x1F0F172A)),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Streak Freezes
                        val spells = userProgress?.streakRestoreSpells ?: 1
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x1F2C2D35) else Color(0x0C0F172A))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFF00C2FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ("Streak Freeze Spells: $spells").localize(),
                                color = Color(0xFF00C2FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                feedbackManager.playSound("typing")
                                showStreakCelebration = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5722)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "KEEP BURNING! 🔥".localize(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
