package com.example.features.smartlearn.presentation

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.ui.components.TickyCard
import com.example.core.ui.components.UniversalFlashcard
import com.example.core.ui.components.toFlashcardData
import com.example.core.ui.components.flashcard.FlashCardState
import com.example.core.learning.*
import com.example.core.database.CardEntity
import com.example.core.database.toWordDetails
import com.example.core.database.WordDetails
import com.example.core.database.DictWord
import com.example.core.database.ReviewHistoryEntity
import com.example.core.database.BoxWordEntity
import com.example.core.fsrs.FsrsCardModel
import com.example.core.fsrs.FsrsRepository
import com.example.core.fsrs.ReviewRatingModel
import com.example.core.fsrs.toFsrsModel
import com.example.core.fsrs.toCardEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSessionScreen(
    navController: NavController,
    isBoxSession: Boolean = false,
    boxId: Int = -1
) {
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val fsrsRepo = remember { FsrsRepository() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val feedbackManager = remember { com.example.core.feedback.FeedbackManager.getInstance(context) }

    // Haptic + Sound Hooks
    fun onCorrectAnswer() {
        Log.d("HapticSoundHook", "Haptic & Sound: Correct Answer Triggered")
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onWrongAnswer() {
        Log.d("HapticSoundHook", "Haptic & Sound: Wrong Answer Triggered")
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun onCardFlip() {
        Log.d("HapticSoundHook", "Haptic & Sound: Card Flip Triggered")
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val userProgress by repo.userProgress.collectAsState(initial = null)
    val rewardHistory by repo.rewardHistory.collectAsState(initial = emptyList())



    var currentCardIndex by remember { mutableStateOf(0) }
    var loadedCards by remember { mutableStateOf<List<Pair<CardEntity, DictWord>>>(emptyList()) }
    var boxName by remember { mutableStateOf("Vocab Box") }
    var boxCards by remember { mutableStateOf<List<BoxWordEntity>>(emptyList()) }
    var totalBoxWordsCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isFlipped by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Session statistics
    var xpEarnedInSession by remember { mutableStateOf(0) }
    var isSessionCompleted by remember { mutableStateOf(false) }
    var showLeveledUpDialog by remember { mutableStateOf(false) }
    var hasLeveledUpInSession by remember { mutableStateOf(false) }
    var displayedXpEarned by remember { mutableIntStateOf(0) }
    var currentAnimatingLevelXp by remember { mutableIntStateOf(-1) }
    val particleProgresses = remember { List(15) { Animatable(0f) } }
    var totalAnswersInSession by remember { mutableStateOf(0) }
    var correctAnswersInSession by remember { mutableStateOf(0) }

    // Tiki reaction state
    var tikiReactionMessage by remember { mutableStateOf("Tiki is watching! Recall correctly to impress me!") }
    var currentStreakCount by remember { mutableStateOf(0) }
    var consecutiveMistakesCount by remember { mutableStateOf(0) }

    var initialReviewLogs by remember { mutableStateOf<List<ReviewHistoryEntity>>(emptyList()) }

    // Setup the unified session queue engine dynamically once cards load
    val engine = remember(loadedCards, boxCards, initialReviewLogs, isBoxSession) {
        if (isBoxSession) {
            if (boxCards.isEmpty()) null else {
                val items = boxCards.map { word ->
                    val wordReviewHistory = initialReviewLogs.filter { it.isBoxReview && it.wordId == word.id }.sortedBy { it.timestamp }
                    val initialCircleStates = List(7) { idx ->
                        if (idx < wordReviewHistory.size) {
                            when (wordReviewHistory[idx].rating) {
                                4 -> "Green"
                                3 -> "Blue"
                                2 -> "Yellow"
                                else -> "Red"
                            }
                        } else "Gray"
                    }
                    StudySessionItem(
                        id = word.id.toString(),
                        data = word.toFlashcardData(),
                        circleStates = initialCircleStates,
                        payload = word
                    )
                }
                val startIdx = currentCardIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
                val queueManager = SessionQueueManager(items)
                repeat(startIdx) { queueManager.next() }
                StudySessionEngine(
                    queueManager = queueManager,
                    scope = coroutineScope,
                    initialStreak = currentStreakCount,
                    onCorrectHook = { onCorrectAnswer() },
                    onWrongHook = { onWrongAnswer() },
                    onSessionFinished = { isSessionCompleted = true }
                )
            }
        } else {
            if (loadedCards.isEmpty()) null else {
                val items = loadedCards.map { (card, word) ->
                    val wordReviewHistory = initialReviewLogs.filter { !it.isBoxReview && it.wordId == word.id }.sortedBy { it.timestamp }
                    val initialCircleStates = List(7) { i ->
                        if (i < wordReviewHistory.size) {
                            val ratingVal = wordReviewHistory[i].rating
                            when (ratingVal) {
                                4 -> "Green"
                                3 -> "Blue"
                                2 -> "Yellow"
                                else -> "Red"
                            }
                        } else "Gray"
                    }
                    StudySessionItem(
                        id = card.id.toString(),
                        data = (card to word).toFlashcardData(),
                        circleStates = initialCircleStates,
                        payload = card to word
                    )
                }
                val startIdx = currentCardIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
                val queueManager = SessionQueueManager(items)
                repeat(startIdx) { queueManager.next() }
                StudySessionEngine(
                    queueManager = queueManager,
                    scope = coroutineScope,
                    initialStreak = currentStreakCount,
                    onCorrectHook = { onCorrectAnswer() },
                    onWrongHook = { onWrongAnswer() },
                    onSessionFinished = { isSessionCompleted = true }
                )
            }
        }
    }

    // Connect queue index & current item flows to Composable state
    val activeIndex by engine?.queueManager?.currentIndex?.collectAsState() ?: remember { mutableStateOf(0) }
    val currentItem by engine?.queueManager?.currentItem?.collectAsState() ?: remember { mutableStateOf(null) }

    val isSessionDone = engine?.isSessionCompleted ?: isSessionCompleted
    val finalLevelXp = userProgress?.xp ?: 0
    val sessionXp = engine?.xpEarned ?: xpEarnedInSession

    LaunchedEffect(isSessionDone, finalLevelXp, sessionXp) {
        if (isSessionDone && sessionXp > 0) {
            val startLevelXp = (finalLevelXp - sessionXp).coerceAtLeast(0)
            currentAnimatingLevelXp = startLevelXp
            displayedXpEarned = 0
            particleProgresses.forEach { it.snapTo(0f) }
            
            delay(1200) // Stately delay before particles start flying
            val context = SevenTicksApplication.instance.applicationContext
            val feedbackManager = com.example.core.feedback.FeedbackManager.getInstance(context)
            
            particleProgresses.forEachIndexed { index, animatable ->
                launch {
                    delay(index * 55L) // Stagger particle launch
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 650, easing = EaseInOutSine)
                    )
                    // Landed!
                    feedbackManager.playSound("xp_gain")
                    feedbackManager.vibrateLight()
                    
                    // Increment numbers step-by-step
                    val progressRatio = (index + 1).toFloat() / 15f
                    displayedXpEarned = (progressRatio * sessionXp).toInt().coerceAtMost(sessionXp)
                    currentAnimatingLevelXp = (startLevelXp + (progressRatio * sessionXp).toInt()).coerceAtMost(finalLevelXp)
                }
            }
            delay(1200)
            // Safety alignment
            displayedXpEarned = sessionXp
            currentAnimatingLevelXp = finalLevelXp
        }
    }

    LaunchedEffect(currentItem) {
        if (!isBoxSession && currentItem != null) {
            repo.smartSessionEngine.timingTracker.start()
        }
    }

    // Animating circle feedback states
    var temporaryOverlayIndex by remember { mutableStateOf(-1) }
    var temporaryOverlayRating by remember { mutableStateOf("") }

    // Load active session card list
    LaunchedEffect(isBoxSession, boxId) {
        isLoading = true
        repo.updateStreakOnActivity()
        initialReviewLogs = repo.getReviewHistoryOnce()

        if (isBoxSession) {
            val box = boxRepo.getCustomBoxById(boxId)
            if (box != null) {
                boxName = box.name
            }
            val list = boxRepo.getWordsInCustomBoxOnce(boxId)
            totalBoxWordsCount = list.size
            val currentTime = System.currentTimeMillis()
            boxCards = list.filter { it.dueDate <= currentTime }
            currentCardIndex = 0
            isLoading = false
        } else {
            val state = repo.getSessionStateOnce()
            if (state != null && state.active && state.cardIds.isNotEmpty()) {
                val ids = state.cardIds.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
                val cardsList = mutableListOf<Pair<CardEntity, DictWord>>()
                for (id in ids) {
                    val card = repo.getCardById(id)
                    if (card != null) {
                        val wordDetails = SevenTicksApplication.instance.vocabDatabaseManager.getWordById(card.wordId)
                        if (wordDetails != null) {
                            cardsList.add(Pair(card, wordDetails))
                        }
                    }
                }
                loadedCards = cardsList
                currentCardIndex = state.currentIndex.coerceIn(0, (cardsList.size - 1).coerceAtLeast(0))
                isLoading = false
            } else {
                isLoading = false
            }
        }
    }

    // Soft moving gradient background animation
    val infiniteTransition = rememberInfiniteTransition(label = "background_flow")
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    val animatedBgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF060713),
            Color(0xFF090D26).copy(alpha = 0.9f + (bgShift / 1000f)),
            Color(0xFF1D0A30).copy(alpha = 0.85f - (bgShift / 1000f)),
            Color(0xFF060713)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f + bgShift * 3f, 1500f - bgShift * 2f)
    )

    // Micro floating motion for the main flashcard
    val cardFloatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_floating"
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgBrush),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00FFD2))
        }
        return
    }

    if (if (isBoxSession) boxCards.isEmpty() else loadedCards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isBoxSession) {
                            if (totalBoxWordsCount == 0) {
                                "هنوز هیچ کلمه‌ای به این باکس اضافه نشده است."
                            } else {
                                "تمامی کلمات این باکس با موفقیت مرور شده‌اند! طبق الگوریتم زمان‌بندی مرور بعدی (FSRS)، در حال حاضر کلمه‌ای برای مرور وجود ندارد."
                            }
                        } else {
                            "No active session found."
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumGlassButton(
                        text = "Go Back",
                        onClick = { navController.popBackStack() }
                    )
                }
            }
        }
        return
    }

    if (engine?.isSessionCompleted ?: isSessionCompleted) {
        // --- SESSION COMPLETION SCREEN & REWARD OVERLAY ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgBrush),
            contentAlignment = Alignment.Center
        ) {
            // Background Confetti Effect
            ConfettiEffect()

            if (rewardHistory.isNotEmpty()) {
                val reward = rewardHistory.first()
                val isLevelUp = reward.type == "LEVEL_UP"
                
                val context = SevenTicksApplication.instance.applicationContext
                val feedbackManager = remember { com.example.core.feedback.FeedbackManager.getInstance(context) }
                
                // Trigger Level Up celebratory sound and vibration loop once
                LaunchedEffect(reward.id) {
                    if (isLevelUp) {
                        feedbackManager.playSound("level_up")
                        feedbackManager.vibrateHeavy()
                        delay(250)
                        feedbackManager.vibrateMedium()
                        delay(250)
                        feedbackManager.vibrateLight()
                    } else {
                        feedbackManager.playSound("streak")
                        feedbackManager.vibrateMedium()
                    }
                }

                val infiniteTransition = rememberInfiniteTransition(label = "reward_anim")
                val badgeRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "badge_rot"
                )

                val badgeScale by infiniteTransition.animateFloat(
                    initialValue = 0.93f,
                    targetValue = 1.07f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "badge_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back glowing radiant aura
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(badgeScale)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isLevelUp) listOf(Color(0x55FFD600), Color.Transparent)
                                    else listOf(Color(0x44E040FB), Color.Transparent)
                                )
                            )
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .scale(1.02f),
                        cornerRadius = 32.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.padding(28.dp)
                        ) {
                            // Centered Level Star Shield or Trophy
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .scale(badgeScale),
                                contentAlignment = Alignment.Center
                            ) {
                                // Rotating outer rays
                                Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = badgeRotation }) {
                                    val w = size.width
                                    val h = size.height
                                    val rayCount = 8
                                    val rayColor = if (isLevelUp) Color(0x33FFD600) else Color(0x22E040FB)
                                    for (i in 0 until rayCount) {
                                        val angle = (i * (360f / rayCount)) * (Math.PI / 180f)
                                        val endX = w / 2 + kotlin.math.cos(angle).toFloat() * (w / 2)
                                        val endY = h / 2 + kotlin.math.sin(angle).toFloat() * (h / 2)
                                        drawCircle(
                                            color = rayColor,
                                            radius = 12.dp.toPx(),
                                            center = Offset(endX, endY)
                                        )
                                    }
                                }

                                // Golden shield with star
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = if (isLevelUp) listOf(Color(0xFFFFEA00), Color(0xFFFF9100))
                                                else listOf(Color(0xFFE040FB), Color(0xFF651FFF))
                                            )
                                        )
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isLevelUp) "🌟 LEVEL UP! 🌟" else "🏅 REWARD EARNED! 🏅",
                                color = if (isLevelUp) Color(0xFFFFD600) else Color(0xFF00FFD2),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = reward.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Persian description and stats
                            Text(
                                text = if (isLevelUp) "سطح جدید با موفقیت باز شد! به تمرینات روزانه خود ادامه دهید تا زنجیره یادگیری خود را پرثمرتر کنید."
                                else "تبریک می‌گوییم! پاداش تلاش‌ها و دستاوردهای یادگیری خود را دریافت کنید.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            // XP Booster Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isLevelUp) Color(0x33FFD600) else Color(0x33E040FB)
                                    )
                                    .border(
                                        1.dp,
                                        if (isLevelUp) Color(0xFFFFD600) else Color(0xFFE040FB),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+${reward.rewardXp} XP Boost",
                                    color = Color(0xFF00FFD2),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            val tikiMsg = when (reward.type) {
                                "LEVEL_UP" -> "Tiki is celebrating your cognitive growth! Let's do a happy island dance! 🌴💃"
                                "CHALLENGE_COMPLETE" -> "Tiki is proud of your persistence! Double high-five! 🐾✋"
                                "ACHIEVEMENT_UNLOCK" -> "Tiki is amazed by your dedication! That is a rare badge! 🏆✨"
                                "STREAK_MILESTONE" -> "Your burning passion is inspiring! Tiki says keep the flame alive! 🔥🦖"
                                else -> "Tiki is cheering for you! Keep up the incredible work!"
                            }

                            TickyCard(
                                message = tikiMsg,
                                sizeDp = 60,
                                modifier = Modifier.fillMaxWidth()
                            )

                            PremiumGlassButton(
                                text = if (isLevelUp) "CLAIM LEVEL REWARD! 🌟" else "Claim Reward",
                                onClick = {
                                    coroutineScope.launch {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        feedbackManager.playSound("typing")
                                        repo.dismissReward(reward.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // --- COMPREHENSIVE SESSION COMPLETION CARD ---
                val calculatedAccuracy = if (totalAnswersInSession > 0) {
                    (correctAnswersInSession.toFloat() / totalAnswersInSession.toFloat() * 100).toInt()
                } else {
                    100
                }

                val currentStreak = userProgress?.streak ?: 1

                val levelNum = userProgress?.level ?: 1
                val targetXpNeeded = levelNum * 500
                
                val levelXpToDisplay = if (currentAnimatingLevelXp == -1) (userProgress?.xp ?: 0) else currentAnimatingLevelXp
                val xpEarnedToDisplay = if (currentAnimatingLevelXp == -1) (engine?.xpEarned ?: xpEarnedInSession) else displayedXpEarned
                val currentAnimatingLevelProgress = if (targetXpNeeded > 0) {
                    levelXpToDisplay.toFloat() / targetXpNeeded.toFloat()
                } else {
                    0f
                }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    cornerRadius = 28.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Session Complete! 🎉",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "You've successfully processed all assigned words for this adaptive smart session.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

                        // Detailed stats row (including Streak & Accuracy!)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+$xpEarnedToDisplay",
                                    color = Color(0xFFFFD600),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "XP Earned",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${loadedCards.size}",
                                    color = Color(0xFF00FFD2),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Reviewed",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$calculatedAccuracy%",
                                    color = Color(0xFF00E676),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Accuracy",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentStreak🔥",
                                    color = Color(0xFFFF7043),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Streak",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

                        // Level progress bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level $levelNum Progress",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$levelXpToDisplay / $targetXpNeeded XP",
                                    color = Color(0xFF00FFD2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { currentAnimatingLevelProgress },
                                color = Color(0xFF9D00FF),
                                trackColor = Color(0x1AFFFFFF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        PremiumGlassButton(
                            text = "Continue",
                            onClick = {
                                coroutineScope.launch {
                                    repo.updateSessionState(active = false, cardIds = emptyList(), currentIndex = 0)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Drawing the flying gold particles layer
                if (sessionXp > 0) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val w = maxWidth
                        val h = maxHeight
                        
                        // Start: around Center-Left near "+XP Earned" column
                        val startX = w * 0.28f
                        val startY = h * 0.53f
                        
                        // Target: around the progress bar area
                        val targetX = w * 0.5f
                        val targetY = h * 0.72f
                        
                        particleProgresses.forEachIndexed { index, animatable ->
                            val p = animatable.value
                            if (p > 0f && p < 1f) {
                                val sinOffset = kotlin.math.sin(p * Math.PI).toFloat()
                                val curX = startX + (targetX - startX) * p - (sinOffset * 40).dp
                                val curY = startY + (targetY - startY) * p - (sinOffset * 90).dp
                                
                                Box(
                                    modifier = Modifier
                                        .offset(x = curX, y = curY)
                                        .size(14.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFFFFF176), Color(0xFFFFD600), Color.Transparent)
                                            )
                                        )
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val activeItem = currentItem ?: if (isBoxSession) {
        if (boxCards.isNotEmpty()) {
            val word = boxCards.first()
            val wordReviewHistory = initialReviewLogs.filter { it.isBoxReview && it.wordId == word.id }.sortedBy { it.timestamp }
            val initialCircleStates = List(7) { idx ->
                if (idx < wordReviewHistory.size) {
                    when (wordReviewHistory[idx].rating) {
                        4 -> "Green"
                        3 -> "Blue"
                        2 -> "Yellow"
                        else -> "Red"
                    }
                } else "Gray"
            }
            StudySessionItem(
                id = word.id.toString(),
                data = word.toFlashcardData(),
                circleStates = initialCircleStates,
                payload = word
            )
        } else null
    } else {
        if (loadedCards.isNotEmpty()) {
            val wordReviewHistory = initialReviewLogs.filter { !it.isBoxReview && it.wordId == loadedCards.first().second.id }.sortedBy { it.timestamp }
            val initialCircleStates = List(7) { i ->
                if (i < wordReviewHistory.size) {
                    val ratingVal = wordReviewHistory[i].rating
                    when (ratingVal) {
                        4 -> "Green"
                        3 -> "Blue"
                        2 -> "Yellow"
                        else -> "Red"
                    }
                } else "Gray"
            }
            StudySessionItem(
                id = loadedCards.first().first.id.toString(),
                data = (loadedCards.first().first to loadedCards.first().second).toFlashcardData(),
                circleStates = initialCircleStates,
                payload = loadedCards.first()
            )
        } else null
    }

    // Active review states calculated on the fly
    val currentCircleIndex = remember(activeItem) {
        val baseCircles = activeItem?.circleStates ?: List(7) { "Gray" }
        baseCircles.count { it != "Gray" }.coerceAtMost(6)
    }
    val circleStatesToShow = remember(activeItem, engine?.temporaryOverlayIndex, engine?.temporaryOverlayRating) {
        val baseCircles = activeItem?.circleStates ?: List(7) { "Gray" }
        val overlayIdx = engine?.temporaryOverlayIndex ?: -1
        val overlayRating = engine?.temporaryOverlayRating ?: ""
        if (overlayIdx in 0..6 && overlayRating.isNotEmpty()) {
            baseCircles.toMutableList().apply {
                this[overlayIdx] = overlayRating
            }
        } else {
            baseCircles
        }
    }

    LaunchedEffect(activeItem) {
        isFlipped = false
    }

    // Handle button action click and transition logic
    fun handleRating(rating: ReviewRatingModel) {
        val activeEngine = engine ?: return
        val item = activeItem ?: return

        // Trigger premium haptic and sound feedback
        when (rating) {
            ReviewRatingModel.AGAIN -> {
                feedbackManager.vibrateHeavy()
                feedbackManager.playSound("again")
            }
            ReviewRatingModel.HARD -> {
                feedbackManager.vibrateMedium()
                feedbackManager.playSound("hard")
            }
            ReviewRatingModel.GOOD -> {
                feedbackManager.vibrateLight()
                feedbackManager.playSound("good")
            }
            ReviewRatingModel.EASY -> {
                feedbackManager.vibrateLight()
                feedbackManager.playSound("easy")
            }
        }

        if (isBoxSession) {
            val boxWord = item.payload as? BoxWordEntity ?: return
            val currentBoxIdx = boxWord.boxIndex
            val nextBoxIdx = when (rating) {
                ReviewRatingModel.AGAIN -> 1
                ReviewRatingModel.HARD -> (currentBoxIdx - 1).coerceAtLeast(1)
                ReviewRatingModel.GOOD -> (currentBoxIdx + 1).coerceAtMost(7)
                ReviewRatingModel.EASY -> (currentBoxIdx + 2).coerceAtMost(7)
            }
            val promotedToBox7 = currentBoxIdx < 7 && nextBoxIdx == 7
            
            activeEngine.submitRating(
                rating = rating,
                currentCircleIndex = (boxWord.boxIndex - 1).coerceIn(0, 6),
                xpAmount = when (rating) {
                    ReviewRatingModel.AGAIN -> 5
                    ReviewRatingModel.HARD -> 10
                    ReviewRatingModel.GOOD -> 15
                    ReviewRatingModel.EASY -> 20
                },
                promotedToBox7 = promotedToBox7,
                onSaveDb = {
                    coroutineScope.launch {
                        val leveledUp = repo.reviewCard(
                            cardId = boxWord.id,
                            isBoxWord = true,
                            rating = rating
                        )
                        if (leveledUp) {
                            hasLeveledUpInSession = true
                            activeEngine.triggerEvent(com.example.core.learning.CompanionEvent.LevelUp)
                        }
                    }
                }
            )
        } else {
            val (card, word) = item.payload as? Pair<CardEntity, DictWord> ?: return
            val currentBoxIdx = card.boxIndex
            val nextBoxIdx = when (rating) {
                ReviewRatingModel.AGAIN -> 1
                ReviewRatingModel.HARD -> (currentBoxIdx - 1).coerceAtLeast(1)
                ReviewRatingModel.GOOD -> (currentBoxIdx + 1).coerceAtMost(7)
                ReviewRatingModel.EASY -> (currentBoxIdx + 2).coerceAtMost(7)
            }
            val promotedToBox7 = currentBoxIdx < 7 && nextBoxIdx == 7

            val xpAmount = when (rating) {
                ReviewRatingModel.AGAIN -> 5
                ReviewRatingModel.HARD -> 10
                ReviewRatingModel.GOOD -> 15
                ReviewRatingModel.EASY -> 20
            }

            activeEngine.submitRating(
                rating = rating,
                currentCircleIndex = currentCircleIndex,
                xpAmount = xpAmount,
                promotedToBox7 = promotedToBox7,
                onSaveDb = {
                    coroutineScope.launch {
                        val leveledUp = repo.reviewCard(
                            cardId = card.id,
                            isBoxWord = false,
                            rating = rating
                        )
                        if (leveledUp) {
                            hasLeveledUpInSession = true
                            activeEngine.triggerEvent(com.example.core.learning.CompanionEvent.LevelUp)
                        }

                        // Update session state in database
                        val nextIdx = (engine?.queueManager?.currentIndex?.value ?: 0) + 1
                        if (nextIdx < loadedCards.size) {
                            repo.updateSessionState(
                                active = true,
                                cardIds = loadedCards.map { it.first.id },
                                currentIndex = nextIdx
                            )
                        } else {
                            repo.updateSessionState(
                                active = false,
                                cardIds = emptyList(),
                                currentIndex = 0
                            )
                        }
                    }
                }
            )
        }
    }

    // Camera 3D rotation rotation angle
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "camera_rotation_y"
    )

    androidx.activity.compose.BackHandler(enabled = true) {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isBoxSession) boxName else "Smart Learn Session",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Display tips context */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Help", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBgBrush)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Session Header Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level ${userProgress?.level ?: 1} | +${engine?.xpEarned ?: xpEarnedInSession} XP",
                        color = Color(0xFFFFD600),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    val total = if (isBoxSession) boxCards.size else loadedCards.size
                    val completedCount = engine?.completedCardsCount ?: 0
                    Text(
                        text = "Card ${completedCount + 1} of $total",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = {
                        val total = if (isBoxSession) boxCards.size else loadedCards.size
                        val completedCount = engine?.completedCardsCount ?: 0
                        if (total > 0) completedCount.toFloat() / total.toFloat() else 0f
                    },
                    color = Color(0xFF00FFD2),
                    trackColor = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                if (!isBoxSession) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // State Breakdown Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val newCount = loadedCards.count { it.first.state == 0 }
                        val learnCount = loadedCards.count { it.first.state == 1 }
                        val relearnCount = loadedCards.count { it.first.state == 3 }
                        val dueCount = loadedCards.count { it.first.state == 2 }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF00E5FF)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "New: $newCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFFFEA00)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Learn: $learnCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFFF9100)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Relearn: $relearnCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF00E676)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Due: $dueCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }

                    // Upcoming Cards Preview
                    val nextItems = engine?.queueManager?.getNextItems(2) ?: emptyList()
                    if (nextItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Next: " + nextItems.joinToString(", ") { item ->
                                val payload = item.payload
                                if (payload is Pair<*, *>) {
                                    (payload.second as? DictWord)?.word ?: ""
                                } else if (payload is BoxWordEntity) {
                                    payload.word
                                } else {
                                    ""
                                }
                            },
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // 2. High-fidelity Unified Universal Spaced Repetition Flashcard
            val wordDetails = remember(activeItem, isBoxSession) {
                if (isBoxSession) {
                    (activeItem?.payload as? BoxWordEntity)?.toWordDetails() ?: WordDetails(word = "", level = "", phonetics = "", definitions = emptyList(), translations = emptyList(), examples = emptyList(), exampleTranslations = emptyList(), synonyms = emptyList(), antonyms = emptyList(), wordFamily = emptyList(), collocations = emptyList(), phrases = emptyList(), notes = emptyList(), types = emptyList(), topics = emptyList())
                } else {
                    val pair = activeItem?.payload as? Pair<CardEntity, DictWord>
                    pair?.second?.toWordDetails() ?: WordDetails(word = "", level = "", phonetics = "", definitions = emptyList(), translations = emptyList(), examples = emptyList(), exampleTranslations = emptyList(), synonyms = emptyList(), antonyms = emptyList(), wordFamily = emptyList(), collocations = emptyList(), phrases = emptyList(), notes = emptyList(), types = emptyList(), topics = emptyList())
                }
            }

            com.example.core.ui.components.flashcard.FlashcardScreen(
                wordDetails = wordDetails,
                isFlipped = isFlipped,
                onFlip = {
                    onCardFlip()
                    isFlipped = !isFlipped
                },
                onRatingClick = { handleRating(it) },
                circleStates = circleStatesToShow,
                tikiMessage = engine?.tikiReactionMessage ?: tikiReactionMessage,
                tikiState = engine?.tikiState ?: "st-happy",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }


}

// Bouncy ActionButton with physical spring scale press state feedback
@Composable
fun ActionButton(
    text: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_bounce"
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                coroutineScope.launch {
                    isPressed = true
                    delay(80)
                    isPressed = false
                }
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotSpeed: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(60) {
            mutableStateOf(
                ConfettiParticle(
                    x = (0..1000).random().toFloat() / 1000f,
                    y = -(0..500).random().toFloat() / 500f,
                    speedY = (8..24).random().toFloat() / 1000f,
                    speedX = (-8..8).random().toFloat() / 1000f,
                    color = Color(
                        red = (120..255).random() / 255f,
                        green = (120..255).random() / 255f,
                        blue = (120..255).random() / 255f,
                        alpha = 1f
                    ),
                    size = (6..16).random().toFloat(),
                    rotation = (0..360).random().toFloat(),
                    rotSpeed = (-6..6).random().toFloat()
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            for (pState in particles) {
                val p = pState.value
                var newY = p.y + p.speedY
                var newX = p.x + p.speedX
                var newRot = p.rotation + p.rotSpeed
                if (newY > 1.1f) {
                    newY = -0.1f
                    newX = (0..1000).random().toFloat() / 1000f
                }
                if (newX < -0.1f) newX = 1.1f
                if (newX > 1.1f) newX = -0.1f
                pState.value = p.copy(y = newY, x = newX, rotation = newRot)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (pState in particles) {
            val p = pState.value
            val px = p.x * w
            val py = p.y * h
            if (py in -100f..h + 100f) {
                drawContext.canvas.save()
                drawContext.canvas.translate(px, py)
                drawContext.canvas.rotate(p.rotation)
                drawRect(
                    color = p.color,
                    topLeft = Offset(-p.size / 2, -p.size / 2),
                    size = Size(p.size, p.size)
                )
                drawContext.canvas.restore()
            }
        }
    }
}
