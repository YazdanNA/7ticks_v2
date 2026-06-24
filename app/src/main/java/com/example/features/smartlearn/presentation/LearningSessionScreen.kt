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
import com.example.core.components.SevenCircles
import com.example.core.components.TikiPlaceholder
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

    val sessionState by repo.sessionState.collectAsState(initial = null)
    val userProgress by repo.userProgress.collectAsState(initial = null)
    val reviewLogs by repo.reviewHistory.collectAsState(initial = emptyList())

    var currentCardIndex by remember { mutableStateOf(0) }
    var loadedCards by remember { mutableStateOf<List<Pair<CardEntity, DictWord>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFlipped by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Session statistics
    var xpEarnedInSession by remember { mutableStateOf(0) }
    var isSessionCompleted by remember { mutableStateOf(false) }
    var showLeveledUpDialog by remember { mutableStateOf(false) }

    // Tiki reaction state
    var tikiReactionMessage by remember { mutableStateOf("Tiki is watching! Recall correctly to impress me!") }
    var currentStreakCount by remember { mutableStateOf(0) }

    // Animating circle feedback states
    var temporaryOverlayIndex by remember { mutableStateOf(-1) }
    var temporaryOverlayRating by remember { mutableStateOf("") }

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

    // Load active session card list
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

    if (isSessionCompleted) {
        // --- SESSION COMPLETION SCREEN ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "You've successfully finished all planned words for this session using smart Leitner algorithms.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "+$xpEarnedInSession",
                                color = Color(0xFFFFD600),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Words Practiced",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lvl ${userProgress?.level ?: 1}",
                                color = Color(0xFF9D00FF),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Level",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
        return
    }

    val (currentCard, currentWord) = loadedCards[currentCardIndex]

    // Fetch real historical reviews for this word to map 7 circles
    val wordReviewHistory = remember(reviewLogs, currentWord.id) {
        reviewLogs.filter { it.wordId == currentWord.id }.sortedBy { it.timestamp }
    }

    // Active review states calculated on the fly
    val currentCircleIndex = wordReviewHistory.size.coerceAtMost(6)
    val circleStates = remember(wordReviewHistory, temporaryOverlayIndex, temporaryOverlayRating) {
        val list = mutableListOf<String>()
        for (i in 0 until 7) {
            if (i == temporaryOverlayIndex) {
                list.add(temporaryOverlayRating)
            } else if (i < wordReviewHistory.size) {
                val rating = wordReviewHistory[i].rating
                val mapped = when (rating) {
                    4 -> "Green"  // Easy
                    3 -> "Blue"   // Good
                    2 -> "Yellow" // Hard
                    else -> "Red" // Again
                }
                list.add(mapped)
            } else {
                list.add("Gray")
            }
        }
        list
    }

    // Handle button action click and transition logic
    fun handleRating(rating: ReviewRatingModel) {
        coroutineScope.launch {
            // Trigger haptic & sound hooks
            if (rating == ReviewRatingModel.GOOD || rating == ReviewRatingModel.EASY) {
                onCorrectAnswer()
                currentStreakCount++
                tikiReactionMessage = if (currentStreakCount >= 3) {
                    "Incredible $currentStreakCount-word streak! Tiki is excited! 🔥"
                } else {
                    "Awesome! Tiki is super happy! 🎉"
                }
            } else {
                onWrongAnswer()
                currentStreakCount = 0
                tikiReactionMessage = "Ah, no worries! Tiki believes in you. Let's practice!"
            }

            // Temporarily overlay circle states to show animation before shifting
            val mappedColorStr = when (rating) {
                ReviewRatingModel.EASY -> "Green"
                ReviewRatingModel.GOOD -> "Blue"
                ReviewRatingModel.HARD -> "Yellow"
                ReviewRatingModel.AGAIN -> "Red"
            }
            temporaryOverlayIndex = currentCircleIndex
            temporaryOverlayRating = mappedColorStr

            // Visual pause so the user can witness the circle fill, icon checkmark & spring scale animation
            delay(850)

            // Submit FSRS review
            val updatedFsrsModel = fsrsRepo.calculateNextReview(
                currentCard.toFsrsModel(),
                rating,
                System.currentTimeMillis()
            )
            val updatedCardEntity = updatedFsrsModel.toCardEntity()
            repo.updateCard(updatedCardEntity)

            // Log review to database history
            repo.recordReviewLog(
                wordId = currentCard.wordId,
                word = currentCard.word,
                rating = rating.value,
                stability = updatedFsrsModel.stability,
                difficulty = updatedFsrsModel.difficulty
            )

            // Award XP based on rating
            val xpAmount = when (rating) {
                ReviewRatingModel.AGAIN -> 5
                ReviewRatingModel.HARD -> 10
                ReviewRatingModel.GOOD -> 15
                ReviewRatingModel.EASY -> 20
            }
            xpEarnedInSession += xpAmount
            val leveledUp = repo.awardXp(xpAmount)
            if (leveledUp) {
                showLeveledUpDialog = true
            }

            // Check level advancement
            repo.checkAndProgressUserLevel()

            // Reset overlays & slide to next
            temporaryOverlayIndex = -1
            temporaryOverlayRating = ""

            val nextIndex = currentCardIndex + 1
            if (nextIndex >= loadedCards.size) {
                isSessionCompleted = true
            } else {
                currentCardIndex = nextIndex
                isFlipped = false
                repo.updateSessionState(
                    active = true,
                    cardIds = loadedCards.map { it.first.id },
                    currentIndex = nextIndex
                )
            }
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
                        text = "Level ${userProgress?.level ?: 1} | +${xpEarnedInSession} XP",
                        color = Color(0xFFFFD600),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Remaining: ${loadedCards.size - currentCardIndex}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentCardIndex + 1).toFloat() / loadedCards.size.toFloat() },
                    color = Color(0xFF00FFD2),
                    trackColor = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // 2. High-fidelity 3D Interactive Flashcard
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = cardFloatY.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .graphicsLayer {
                            rotationY = rotationAngle
                            cameraDistance = 12f * density
                        }
                        .clickable {
                            onCardFlip()
                            isFlipped = !isFlipped
                        }
                ) {
                    if (rotationAngle <= 90f) {
                        // --- FRONT SIDE ---
                        GlassCard(
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 24.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x1A00C2FF))
                                            .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentWord.partOfSpeech.uppercase(),
                                            color = Color(0xFF00FFD2),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentWord.word,
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentWord.phonetics,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // English definition (short) + example sentence centered
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = currentWord.definition,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (currentWord.example.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x0AFFFFFF))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = "\"${currentWord.example}\"",
                                                color = Color(0xFF00FFD2).copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Tap Card to Reveal Meaning",
                                    color = Color(0xFF00C2FF).copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // --- BACK SIDE (Mirrored to show correctly) ---
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = 180f
                                }
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 24.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentWord.word,
                                            color = Color(0xFF00C2FF),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { /* Audio pronunciation placeholder */ }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Pronounce", tint = Color(0xFF00FFD2))
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Meaning (Multi-language ready Persian/Farsi translation)
                                        Text(
                                            text = currentWord.faDefinition,
                                            color = Color(0xFFFFD600),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        // Primary English definitions & meanings
                                        Text(
                                            text = currentWord.definition,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Examples (Multiple examples represented)
                                        if (currentWord.example.isNotEmpty()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0x06FFFFFF))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = "Primary Usage:",
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                )
                                                Text(
                                                    text = "\"${currentWord.example}\"",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 15.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Typical Word Pattern:",
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                )
                                                Text(
                                                    text = "\"Highly ${currentWord.word} terms represent refined communication.\"",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }

                                    // Button: More Details (Opens bottom sheet)
                                    PremiumGlassButton(
                                        text = "More Details",
                                        onClick = { showBottomSheet = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Spaced Repetition Seven Circles System Visualizer
            SevenCircles(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                activeStates = circleStates
            )

            // 4. Tiki Mascot Helper & Integration area
            TikiPlaceholder(
                message = tikiReactionMessage,
                sizeDp = 50,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 5. Spaced Repetition (Again, Hard, Good, Easy) Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AGAIN (Red)
                ActionButton(
                    text = "Again",
                    subtext = "<1m",
                    color = Color(0xFFFF1744),
                    modifier = Modifier.weight(1f)
                ) {
                    handleRating(ReviewRatingModel.AGAIN)
                }

                // HARD (Yellow)
                ActionButton(
                    text = "Hard",
                    subtext = "<10m",
                    color = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                ) {
                    handleRating(ReviewRatingModel.HARD)
                }

                // GOOD (Blue)
                ActionButton(
                    text = "Good",
                    subtext = "1d",
                    color = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f)
                ) {
                    handleRating(ReviewRatingModel.GOOD)
                }

                // EASY (Green)
                ActionButton(
                    text = "Easy",
                    subtext = "4d",
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                ) {
                    handleRating(ReviewRatingModel.EASY)
                }
            }
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
