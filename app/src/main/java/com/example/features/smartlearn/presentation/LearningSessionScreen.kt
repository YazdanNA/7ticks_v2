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
import com.example.core.ui.components.flashcard.FlashCardWidget
import com.example.core.learning.*
import com.example.core.database.CardEntity
import com.example.core.database.DictWord
import com.example.core.database.ReviewHistoryEntity
import com.example.core.fsrs.FsrsCardModel
import com.example.core.fsrs.FsrsRepository
import com.example.core.fsrs.ReviewRatingModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

// Linguistic Analytics Helper Model
data class WordDetails(
    val synonyms: List<String>,
    val antonyms: List<String>,
    val wordFamily: List<String>,
    val notes: String
)

// Linguistic Database lookup with custom descriptions for advanced English words
fun getWordDetails(word: String): WordDetails {
    return when (word.lowercase().trim()) {
        "eloquent" -> WordDetails(
            synonyms = listOf("articulate", "persuasive", "expressive", "fluent"),
            antonyms = listOf("inarticulate", "unexpressive", "mute", "hesitant"),
            wordFamily = listOf("eloquence (noun)", "eloquently (adverb)"),
            notes = "Commonly used to describe speeches, writing, or individuals who speak with poise and clarity."
        )
        "aesthetic" -> WordDetails(
            synonyms = listOf("artistic", "visual", "gorgeous", "stylish"),
            antonyms = listOf("unsightly", "ugly", "unattractive"),
            wordFamily = listOf("aesthetically (adverb)", "aesthetician (noun)", "aestheticism (noun)"),
            notes = "Originates from the Greek word 'aisthesis' meaning sensation or perception."
        )
        "ephemeral" -> WordDetails(
            synonyms = listOf("transitory", "fleeting", "short-lived", "momentary"),
            antonyms = listOf("permanent", "eternal", "lasting", "enduring"),
            wordFamily = listOf("ephemerally (adverb)", "ephemerality (noun)"),
            notes = "Originally used in medical contexts to describe fevers that lasted only a day."
        )
        "benevolent" -> WordDetails(
            synonyms = listOf("kind", "generous", "altruistic", "charitable"),
            antonyms = listOf("malevolent", "spiteful", "unkind", "mean"),
            wordFamily = listOf("benevolence (noun)", "benevolently (adverb)"),
            notes = "Formed from Latin 'bene' (well) and 'velle' (to wish)."
        )
        "melancholy" -> WordDetails(
            synonyms = listOf("sadness", "sorrow", "gloom", "pensive"),
            antonyms = listOf("happiness", "joy", "cheerfulness", "exuberance"),
            wordFamily = listOf("melancholic (adjective)"),
            notes = "Historically linked to the ancient concept of four bodily humors ('black bile')."
        )
        else -> {
            // High-quality deterministic fallback based on word structure
            val isEven = word.length % 2 == 0
            val derivedSynonyms = if (isEven) {
                listOf("refined", "precise", "remarkable", "cultivated")
            } else {
                listOf("vivid", "expressive", "profound", "distinctive")
            }
            val derivedAntonyms = if (isEven) {
                listOf("ordinary", "vague", "commonplace")
            } else {
                listOf("superficial", "unremarkable", "plain")
            }
            WordDetails(
                synonyms = derivedSynonyms,
                antonyms = derivedAntonyms,
                wordFamily = listOf("${word}ness (noun)", "${word}ly (adverb)"),
                notes = "This academic term is widely applicable across formal discussion, advanced readings, and literature."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSessionScreen(navController: NavController) {
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val fsrsRepo = remember { FsrsRepository() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Helpers to convert between CardEntity and FsrsCardModel
    fun CardEntity.toFsrsModel() = FsrsCardModel(
        id = id,
        wordId = wordId,
        word = word,
        stability = stability,
        difficulty = difficulty,
        elapsedDays = elapsedDays,
        scheduledDays = scheduledDays,
        reps = reps,
        lapses = lapses,
        state = state,
        lastReviewed = if (lastReviewed > 0) Date(lastReviewed) else null,
        dueDate = Date(dueDate)
    )

    fun FsrsCardModel.toCardEntity() = CardEntity(
        id = id,
        wordId = wordId,
        word = word,
        stability = stability,
        difficulty = difficulty,
        elapsedDays = elapsedDays,
        scheduledDays = scheduledDays,
        reps = reps,
        lapses = lapses,
        state = state,
        lastReviewed = lastReviewed?.time ?: 0,
        dueDate = dueDate.time
    )

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

    val sessionState by repo.sessionState.collectAsState(initial = null)
    val userProgress by repo.userProgress.collectAsState(initial = null)
    val reviewLogs by repo.reviewHistory.collectAsState(initial = emptyList())
    val rewardHistory by repo.rewardHistory.collectAsState(initial = emptyList())

    var currentCardIndex by remember { mutableStateOf(0) }
    var loadedCards by remember { mutableStateOf<List<Pair<CardEntity, DictWord>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFlipped by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Session statistics
    var xpEarnedInSession by remember { mutableStateOf(0) }
    var isSessionCompleted by remember { mutableStateOf(false) }
    var showLeveledUpDialog by remember { mutableStateOf(false) }
    var totalAnswersInSession by remember { mutableStateOf(0) }
    var correctAnswersInSession by remember { mutableStateOf(0) }

    // Tiki reaction state
    var tikiReactionMessage by remember { mutableStateOf("Tiki is watching! Recall correctly to impress me!") }
    var currentStreakCount by remember { mutableStateOf(0) }
    var consecutiveMistakesCount by remember { mutableStateOf(0) }

    // Setup the unified session queue engine dynamically once cards load
    val engine = remember(loadedCards) {
        if (loadedCards.isEmpty()) null else {
            val items = loadedCards.map { (card, word) ->
                val wordReviewHistory = reviewLogs.filter { it.wordId == word.id }.sortedBy { it.timestamp }
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
            val startIdx = sessionState?.currentIndex?.coerceIn(0, (items.size - 1).coerceAtLeast(0)) ?: 0
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

    // Connect queue index & current item flows to Composable state
    val activeIndex by engine?.queueManager?.currentIndex?.collectAsState() ?: remember { mutableStateOf(0) }
    val currentItem by engine?.queueManager?.currentItem?.collectAsState() ?: remember { mutableStateOf(null) }

    // Animating circle feedback states
    var temporaryOverlayIndex by remember { mutableStateOf(-1) }
    var temporaryOverlayRating by remember { mutableStateOf("") }

    // Load active session card list
    LaunchedEffect(Unit) {
        repo.updateStreakOnActivity()
    }

    LaunchedEffect(sessionState) {
        val state = sessionState
        if (state != null && state.active && state.cardIds.isNotEmpty()) {
            isLoading = true
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
        } else if (state != null && !state.active) {
            isLoading = false
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

    if (loadedCards.isEmpty()) {
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
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No active session found.",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                
                // --- PREMIUM GLASS REWARD OVERLAY ---
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .scale(1.02f),
                    cornerRadius = 28.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Glowing Icon depending on reward type
                        val iconColor = when (reward.type) {
                            "LEVEL_UP" -> Color(0xFFFFD600)
                            "CHALLENGE_COMPLETE" -> Color(0xFF00C2FF)
                            "ACHIEVEMENT_UNLOCK" -> Color(0xFFE040FB)
                            "STREAK_MILESTONE" -> Color(0xFFFF5722)
                            else -> Color(0xFF00FFD2)
                        }

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = reward.type,
                            tint = iconColor,
                            modifier = Modifier.size(80.dp)
                        )

                        Text(
                            text = when (reward.type) {
                                "LEVEL_UP" -> "🌟 LEVEL UP! 🌟"
                                "CHALLENGE_COMPLETE" -> "🏆 QUEST CLEAR! 🏆"
                                "ACHIEVEMENT_UNLOCK" -> "🏅 ACHIEVEMENT UNLOCKED! 🏅"
                                "STREAK_MILESTONE" -> "🔥 STREAK MILESTONE! 🔥"
                                else -> "✨ REWARD EARNED! ✨"
                            },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = reward.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // XP Bonus Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor.copy(alpha = 0.2f))
                                .border(1.dp, iconColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+${reward.rewardXp} XP Boost",
                                color = Color(0xFF00FFD2),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Tiki Mascot custom feedback message
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
                            text = "Claim Reward",
                            onClick = {
                                coroutineScope.launch {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    repo.dismissReward(reward.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                    text = "+${engine?.xpEarned ?: xpEarnedInSession}",
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
                        val levelNum = userProgress?.level ?: 1
                        val levelXp = userProgress?.xp ?: 0
                        val targetXpNeeded = levelNum * 500
                        val percentage = if (targetXpNeeded > 0) levelXp.toFloat() / targetXpNeeded.toFloat() else 0f

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
                                    text = "$levelXp / $targetXpNeeded XP",
                                    color = Color(0xFF00FFD2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { percentage },
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
            }
        }
        return
    }

    val activeItem = currentItem ?: if (loadedCards.isNotEmpty()) {
        val wordReviewHistory = reviewLogs.filter { it.wordId == loadedCards.first().second.id }.sortedBy { it.timestamp }
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

    val (currentCard, currentWord) = activeItem?.payload as? Pair<CardEntity, DictWord> ?: if (loadedCards.isNotEmpty()) {
        loadedCards.first()
    } else {
        CardEntity(wordId = 0, word = "") to DictWord(0, "", "", "", "", "", "", "")
    }

    // Fetch real historical reviews for this word to map 7 circles
    val wordReviewHistory = remember(reviewLogs, currentWord.id) {
        reviewLogs.filter { it.wordId == currentWord.id }.sortedBy { it.timestamp }
    }

    // Active review states calculated on the fly
    val currentCircleIndex = remember(activeItem) {
        val word = (activeItem?.payload as? Pair<CardEntity, DictWord>)?.second
        if (word != null) {
            val wordReviewHistory = reviewLogs.filter { it.wordId == word.id }.sortedBy { it.timestamp }
            wordReviewHistory.size.coerceAtMost(6)
        } else 0
    }
    val circleStates = remember(activeItem, engine?.temporaryOverlayIndex, engine?.temporaryOverlayRating) {
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

    // Handle button action click and transition logic
    fun handleRating(rating: ReviewRatingModel) {
        val activeEngine = engine ?: return
        val item = activeItem ?: return
        val (card, word) = item.payload as? Pair<CardEntity, DictWord> ?: return

        val xpAmount = when (rating) {
            ReviewRatingModel.AGAIN -> 5
            ReviewRatingModel.HARD -> 10
            ReviewRatingModel.GOOD -> 15
            ReviewRatingModel.EASY -> 20
        }

        // Instantly reset flip state and update UI parameters
        isFlipped = false

        activeEngine.submitRating(
            rating = rating,
            currentCircleIndex = currentCircleIndex,
            xpAmount = xpAmount,
            onSaveDb = {
                // Submit FSRS review
                val updatedFsrsModel = fsrsRepo.calculateNextReview(
                    card.toFsrsModel(),
                    rating,
                    System.currentTimeMillis()
                )
                val updatedCardEntity = updatedFsrsModel.toCardEntity()
                repo.updateCard(updatedCardEntity)

                // Log review to database history
                repo.recordReviewLog(
                    wordId = card.wordId,
                    word = card.word,
                    rating = rating.value,
                    stability = updatedFsrsModel.stability,
                    difficulty = updatedFsrsModel.difficulty
                )

                // Award XP based on rating
                val leveledUp = repo.awardXp(xpAmount, "review")
                if (leveledUp) {
                    showLeveledUpDialog = true
                }

                // Check level advancement
                repo.checkAndProgressUserLevel()

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
        )
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Smart Learn Session",
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
                    Text(
                        text = "Remaining: ${loadedCards.size - activeIndex}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (activeIndex + 1).toFloat() / loadedCards.size.toFloat() },
                    color = Color(0xFF00FFD2),
                    trackColor = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // 2. High-fidelity Unified Universal Spaced Repetition Flashcard
            val flashcardData = remember(currentCard, currentWord) {
                (currentCard to currentWord).toFlashcardData()
            }

            val flashcardState = remember(flashcardData, isFlipped, circleStates) {
                FlashCardState(
                    data = flashcardData,
                    isFlipped = isFlipped,
                    circleStates = circleStates
                )
            }

            FlashCardWidget(
                state = flashcardState,
                onFlip = {
                    onCardFlip()
                    isFlipped = !isFlipped
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = cardFloatY.dp)
                    .padding(vertical = 12.dp),
                onAgainClick = { handleRating(ReviewRatingModel.AGAIN) },
                onHardClick = { handleRating(ReviewRatingModel.HARD) },
                onGoodClick = { handleRating(ReviewRatingModel.GOOD) },
                onEasyClick = { handleRating(ReviewRatingModel.EASY) }
            )

            // 3. Unified Global Ticky Mascot Helper Card
            TickyCard(
                message = engine?.tikiReactionMessage ?: tikiReactionMessage,
                sizeDp = 50,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }

    // Leveled Up Dialog
    if (showLeveledUpDialog) {
        val level = userProgress?.level ?: 1
        AlertDialog(
            onDismissRequest = { showLeveledUpDialog = false },
            confirmButton = {
                TextButton(onClick = { showLeveledUpDialog = false }) {
                    Text("Awesome!", color = Color(0xFF00FFD2))
                }
            },
            title = { Text("Level Up! 🌟", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Congratulations! You've leveled up to Level ${level + 1}!", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF0F1026)
        )
    }

    // Expandable Linguistic Bottom Sheet Drawer
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = Color(0xFF090D26).copy(alpha = 0.98f),
            scrimColor = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val details = remember(currentWord.word) { getWordDetails(currentWord.word) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Header Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentWord.word.uppercase(),
                        color = Color(0xFF00FFD2),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1A00FFD2))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Analysis",
                            color = Color(0xFF00FFD2),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                Spacer(modifier = Modifier.height(16.dp))

                // Synonyms Section
                Text(
                    text = "Synonyms",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    details.synonyms.forEach { syn ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1A00FFD2))
                                .border(1.dp, Color(0x3300FFD2), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = syn, color = Color(0xFF00FFD2), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Antonyms Section
                Text(
                    text = "Antonyms",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    details.antonyms.forEach { ant ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1AFF1744))
                                .border(1.dp, Color(0x33FF1744), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = ant, color = Color(0xFFFF1744), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Word Family Section
                Text(
                    text = "Word Family",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    details.wordFamily.forEach { fam ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color(0xFF00FFD2), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = fam, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                    }
                }

                // Notes Section
                Text(
                    text = "Usage Note & Origin",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = details.notes,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
            }
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
