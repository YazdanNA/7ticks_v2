package com.example.features.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder
import com.example.core.components.AvatarManager
import com.example.core.assessment.AssessmentSession
import com.example.core.assessment.AssessmentItem
import com.example.core.database.SetupStep
import com.example.core.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val context = LocalContext.current

    // Steps state
    var step by remember { mutableStateOf(0) }
    var isTransitioning by remember { mutableStateOf(false) }

    // User configuration states
    var userName by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("") }
    var nativeLanguage by remember { mutableStateOf("Persian") }
    var targetLanguage by remember { mutableStateOf("English") }
    var dailyGoal by remember { mutableStateOf("10 minutes / day") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00") }
    var placementLevel by remember { mutableStateOf("A1") }

    // Adaptive assessment states
    var assessmentSession by remember { mutableStateOf<AssessmentSession?>(null) }
    var currentQuestion by remember { mutableStateOf<AssessmentItem?>(null) }
    var assessmentResultBreakdown by remember { mutableStateOf("") }
    var assessmentFinished by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var assessmentTikiState by remember { mutableStateOf("st-poker") }

    // Environment setup state
    var setupState by remember { mutableStateOf<SetupStep>(SetupStep.Idle) }

    val totalSteps = 9

    // Smooth staggered exit/entrance step management
    fun transitionToStep(newStep: Int) {
        if (isTransitioning) return
        coroutineScope.launch {
            isTransitioning = true
            delay(320) // Let current step exit
            step = newStep
            isTransitioning = false // Let new step enter
        }
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070814),
            Color(0xFF0F1026),
            Color(0xFF1B0B2E)
        )
    )

    // Run setup flow when reaching step 8
    LaunchedEffect(step) {
        if (step == 8) {
            prefs.userName = userName.ifEmpty { "Master Learner" }
            prefs.avatar = selectedAvatar
            prefs.nativeLanguage = nativeLanguage
            prefs.targetLanguage = targetLanguage
            prefs.dailyGoal = dailyGoal
            prefs.reminderTime = if (reminderEnabled) reminderTime else "Disabled"
            
            val mappedLevel = when (placementLevel) {
                "A1" -> 1
                "A2" -> 2
                "B1" -> 3
                "B2" -> 4
                "C1" -> 5
                "C2" -> 6
                else -> 1
            }
            prefs.currentLevel = mappedLevel

            repo.runEnvironmentSetup().collectLatest { state ->
                setupState = state
            }
        }
    }

    // Reset Tiki's assessment face back to neutral after showing feedback
    LaunchedEffect(currentQuestion) {
        if (currentQuestion != null) {
            delay(1200)
            if (assessmentSession != null && !assessmentFinished) {
                assessmentTikiState = "st-poker"
            }
        }
    }

    // Single screen conversational layout
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SevenTicks Onboarding",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step dots indicator (Fixed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until totalSteps) {
                    val active = i <= step
                    val color = if (active) Color(0xFF00C2FF) else Color(0x33FFFFFF)
                    val width = if (i == step) 20.dp else 6.dp
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(width)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }

            // 1. Ticky Mascot (Permanently fixed in place)
            val targetTikiState = when (step) {
                0 -> "st-welcome"
                1 -> "st-talking"
                2 -> "st-happy"
                3 -> "st-happy"
                4 -> "st-happy"
                5 -> "st-happy"
                6 -> "st-happy"
                7 -> {
                    if (assessmentSession == null) "st-happy"
                    else if (assessmentFinished) "st-welcome"
                    else assessmentTikiState
                }
                8 -> {
                    when (setupState) {
                        is SetupStep.Success -> "st-welcome"
                        is SetupStep.Failure -> "st-sad"
                        else -> "st-loading-data"
                    }
                }
                else -> "st-happy"
            }

            TikiPlaceholder(
                tikiState = targetTikiState,
                sizeDp = 90,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 2. Typewriter Message Area (Permanently fixed in place)
            val targetMessage = when (step) {
                0 -> "Hi there! I am Tiki, your vocabulary mentor. Welcome to 7Ticks, a scientifically proven spaced repetition system!"
                1 -> "Let's get to know you! What is your name?"
                2 -> "Choose an avatar that fits your style! It will represent your cognitive profile."
                3 -> "Select your native language so I can customize translations and learning aids."
                4 -> "Select the target language you want to master!"
                5 -> "How much time would you like to dedicate to learning daily? Let's build a strong habit."
                6 -> "Set a daily reminder time so we don't break your learning streak!"
                7 -> {
                    if (assessmentSession == null) "Let's run a short adaptive assessment to determine your starting vocabulary level!"
                    else if (assessmentFinished) "Excellent job! I have fully mapped your starting neural vocabulary profile."
                    else "I'm assessing your skill with every answer. Focus up, you've got this!"
                }
                8 -> {
                    when (setupState) {
                        is SetupStep.Success -> "Woohoo! Your local environment setup is perfect. Let's begin our mastery journey!"
                        is SetupStep.Failure -> "Oh no, there was an error in initializing the database. Let's retry."
                        else -> "Setting up SevenTicks... Downloading your dynamic language database now."
                    }
                }
                else -> ""
            }

            var displayedMessage by remember { mutableStateOf("") }
            LaunchedEffect(targetMessage) {
                if (displayedMessage.isNotEmpty()) {
                    val currentText = displayedMessage
                    for (i in currentText.length downTo 0) {
                        displayedMessage = currentText.substring(0, i)
                        delay(12)
                    }
                }
                delay(120)
                for (char in targetMessage) {
                    displayedMessage += char
                    delay(18)
                }
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp),
                cornerRadius = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayedMessage,
                        color = Color(0xFF00FFD2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Dynamic Content Area (Fills space, compact content elements stagger in/out)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    0 -> {
                        // Welcome View
                        StaggeredItem(index = 0, totalItems = 1, isExiting = isTransitioning) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Remember Forever",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Master your target language vocabulary using our cognitive-optimized Leitner spaced-repetition scheduler. Just 7 successful ticks and it's permanent.",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Name Entry View
                        StaggeredItem(index = 0, totalItems = 1, isExiting = isTransitioning) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = userName,
                                        onValueChange = { userName = it },
                                        placeholder = { Text("Type your name here...", color = Color.White.copy(alpha = 0.4f)) },
                                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00C2FF),
                                            unfocusedBorderColor = Color(0x33FFFFFF),
                                            cursorColor = Color(0xFF00C2FF),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Avatar Grid View (Compact, 3-3-1, All 7 visible simultaneously, No Scrolling)
                        val avatars = remember { AvatarManager.getAvailableAvatars(context) }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0..2) {
                                    if (i < avatars.size) {
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            AvatarGridItem(
                                                avatar = avatars[i],
                                                isSelected = selectedAvatar == avatars[i].id,
                                                index = i,
                                                totalItems = 7,
                                                isExiting = isTransitioning
                                            ) {
                                                selectedAvatar = avatars[i].id
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 3..5) {
                                    if (i < avatars.size) {
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            AvatarGridItem(
                                                avatar = avatars[i],
                                                isSelected = selectedAvatar == avatars[i].id,
                                                index = i,
                                                totalItems = 7,
                                                isExiting = isTransitioning
                                            ) {
                                                selectedAvatar = avatars[i].id
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (6 < avatars.size) {
                                    Box(modifier = Modifier.width(90.dp), contentAlignment = Alignment.Center) {
                                        AvatarGridItem(
                                            avatar = avatars[6],
                                            isSelected = selectedAvatar == avatars[6].id,
                                            index = 6,
                                            totalItems = 7,
                                            isExiting = isTransitioning
                                        ) {
                                            selectedAvatar = avatars[6].id
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Native Language View (Compact Grid, No Scrolling, No search)
                        val nativeLanguages = listOf(
                            "Persian" to true,
                            "English" to false,
                            "German" to false,
                            "French" to false
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = nativeLanguages[0].first,
                                        available = nativeLanguages[0].second,
                                        isSelected = nativeLanguage == nativeLanguages[0].first,
                                        index = 0,
                                        totalItems = 4,
                                        isExiting = isTransitioning
                                    ) { nativeLanguage = nativeLanguages[0].first }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = nativeLanguages[1].first,
                                        available = nativeLanguages[1].second,
                                        isSelected = nativeLanguage == nativeLanguages[1].first,
                                        index = 1,
                                        totalItems = 4,
                                        isExiting = isTransitioning
                                    ) { nativeLanguage = nativeLanguages[1].first }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = nativeLanguages[2].first,
                                        available = nativeLanguages[2].second,
                                        isSelected = nativeLanguage == nativeLanguages[2].first,
                                        index = 2,
                                        totalItems = 4,
                                        isExiting = isTransitioning
                                    ) { nativeLanguage = nativeLanguages[2].first }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = nativeLanguages[3].first,
                                        available = nativeLanguages[3].second,
                                        isSelected = nativeLanguage == nativeLanguages[3].first,
                                        index = 3,
                                        totalItems = 4,
                                        isExiting = isTransitioning
                                    ) { nativeLanguage = nativeLanguages[3].first }
                                }
                            }
                        }
                    }
                    4 -> {
                        // Target Language View (Compact Grid, No Scrolling, No search)
                        val targetLanguages = listOf(
                            "English" to true,
                            "German" to false,
                            "French" to false,
                            "Spanish" to false,
                            "Japanese" to false
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = targetLanguages[0].first,
                                        available = targetLanguages[0].second,
                                        isSelected = targetLanguage == targetLanguages[0].first,
                                        index = 0,
                                        totalItems = 5,
                                        isExiting = isTransitioning
                                    ) { targetLanguage = targetLanguages[0].first }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = targetLanguages[1].first,
                                        available = targetLanguages[1].second,
                                        isSelected = targetLanguage == targetLanguages[1].first,
                                        index = 1,
                                        totalItems = 5,
                                        isExiting = isTransitioning
                                    ) { targetLanguage = targetLanguages[1].first }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = targetLanguages[2].first,
                                        available = targetLanguages[2].second,
                                        isSelected = targetLanguage == targetLanguages[2].first,
                                        index = 2,
                                        totalItems = 5,
                                        isExiting = isTransitioning
                                    ) { targetLanguage = targetLanguages[2].first }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LanguageOptionCard(
                                        name = targetLanguages[3].first,
                                        available = targetLanguages[3].second,
                                        isSelected = targetLanguage == targetLanguages[3].first,
                                        index = 3,
                                        totalItems = 5,
                                        isExiting = isTransitioning
                                    ) { targetLanguage = targetLanguages[3].first }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                                    LanguageOptionCard(
                                        name = targetLanguages[4].first,
                                        available = targetLanguages[4].second,
                                        isSelected = targetLanguage == targetLanguages[4].first,
                                        index = 4,
                                        totalItems = 5,
                                        isExiting = isTransitioning
                                    ) { targetLanguage = targetLanguages[4].first }
                                }
                            }
                        }
                    }
                    5 -> {
                        // Daily Goals Study Time View (Compact Grid, No scrolling, No oversized cards)
                        val goals = listOf(
                            "5 min" to "Casual",
                            "10 min" to "Regular",
                            "15 min" to "Serious",
                            "20 min" to "Intense",
                            "30 min" to "Insane",
                            "45 min" to "Mastery"
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[0].first,
                                        desc = goals[0].second,
                                        isSelected = dailyGoal.startsWith(goals[0].first),
                                        index = 0,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[0].first} / day" }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[1].first,
                                        desc = goals[1].second,
                                        isSelected = dailyGoal.startsWith(goals[1].first),
                                        index = 1,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[1].first} / day" }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[2].first,
                                        desc = goals[2].second,
                                        isSelected = dailyGoal.startsWith(goals[2].first),
                                        index = 2,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[2].first} / day" }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[3].first,
                                        desc = goals[3].second,
                                        isSelected = dailyGoal.startsWith(goals[3].first),
                                        index = 3,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[3].first} / day" }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[4].first,
                                        desc = goals[4].second,
                                        isSelected = dailyGoal.startsWith(goals[4].first),
                                        index = 4,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[4].first} / day" }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    GoalOptionCard(
                                        goalStr = goals[5].first,
                                        desc = goals[5].second,
                                        isSelected = dailyGoal.startsWith(goals[5].first),
                                        index = 5,
                                        totalItems = 6,
                                        isExiting = isTransitioning
                                    ) { dailyGoal = "${goals[5].first} / day" }
                                }
                            }
                        }
                    }
                    6 -> {
                        // iOS-style custom snap wheel reminder picker
                        OnboardingReminderView(
                            enabled = reminderEnabled,
                            selectedTime = reminderTime,
                            isExiting = isTransitioning,
                            onToggle = { reminderEnabled = it },
                            onTimeSelect = { h, m -> reminderTime = "$h:$m" }
                        )
                    }
                    7 -> {
                        // Interactive Adaptive Placement Assessment
                        OnboardingPlacementView(
                            assessmentSession = assessmentSession,
                            currentQuestion = currentQuestion,
                            finished = assessmentFinished,
                            breakdown = assessmentResultBreakdown,
                            isExiting = isTransitioning,
                            onStart = {
                                val session = AssessmentSession()
                                assessmentSession = session
                                currentQuestion = session.getCurrentQuestion()
                                assessmentTikiState = "st-poker"
                            },
                            onAnswer = { idx ->
                                val session = assessmentSession
                                if (session != null && currentQuestion != null) {
                                    val isCorrect = session.submitAnswer(idx)
                                    lastAnswerCorrect = isCorrect
                                    assessmentTikiState = if (isCorrect) "st-welcome" else "st-sad"
                                    
                                    if (session.isComplete) {
                                        val res = session.getResult()
                                        if (res != null) {
                                            placementLevel = res.cefrLevel.label
                                            assessmentResultBreakdown = res.detailedBreakdown
                                        }
                                        assessmentFinished = true
                                        assessmentTikiState = "st-welcome"
                                    } else {
                                        currentQuestion = session.getCurrentQuestion()
                                    }
                                }
                            },
                            onSkip = {
                                placementLevel = "A1"
                                transitionToStep(8)
                            },
                            onProceed = {
                                transitionToStep(8)
                            }
                        )
                    }
                    8 -> {
                        // Database Setup and Progress Screen
                        OnboardingSetupView(
                            setupState = setupState,
                            isExiting = isTransitioning,
                            onRetry = {
                                coroutineScope.launch {
                                    repo.runEnvironmentSetup().collectLatest { state ->
                                        setupState = state
                                    }
                                }
                            },
                            onComplete = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Fixed Navigation Buttons (Always fixed at bottom)
            val isNextEnabled = when (step) {
                1 -> userName.trim().isNotEmpty()
                2 -> selectedAvatar.isNotEmpty()
                3 -> nativeLanguage.isNotEmpty()
                4 -> targetLanguage.isNotEmpty()
                5 -> dailyGoal.isNotEmpty()
                6 -> reminderTime.isNotEmpty()
                else -> true
            }

            if (step < 8) {
                if (step == 0) {
                    PremiumGlassButton(
                        text = "Continue",
                        onClick = { transitionToStep(1) },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (step == 7) {
                    // Placement handles its own navigation states inside OnboardingPlacementView
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { transitionToStep(step - 1) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(0.4f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        PremiumGlassButton(
                            text = "Next",
                            enabled = isNextEnabled,
                            onClick = {
                                when (step) {
                                    1 -> prefs.userName = userName
                                    2 -> prefs.avatar = selectedAvatar
                                    3 -> prefs.nativeLanguage = nativeLanguage
                                    4 -> prefs.targetLanguage = targetLanguage
                                    5 -> prefs.dailyGoal = dailyGoal
                                    6 -> prefs.reminderTime = if (reminderEnabled) reminderTime else "Disabled"
                                }
                                transitionToStep(step + 1)
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ── Custom Helper Composables ──────────────────────────────────────────

@Composable
fun StaggeredItem(
    index: Int,
    totalItems: Int,
    isExiting: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val entranceDelay = index * 60
    val exitDelay = (totalItems - 1 - index).coerceAtLeast(0) * 50
    
    val animState = remember { Animatable(1f) } // 1f = Initial/off-screen right

    LaunchedEffect(isExiting) {
        if (isExiting) {
            delay(exitDelay.toLong())
            animState.animateTo(
                targetValue = -1f, // exit left
                animationSpec = tween(durationMillis = 200, easing = EaseInQuad)
            )
        } else {
            animState.snapTo(1f)
            delay(entranceDelay.toLong())
            animState.animateTo(
                targetValue = 0f, // active rest state
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val offsetWidth = 240.dp.toPx()
                translationX = animState.value * offsetWidth
                alpha = 1f - kotlin.math.abs(animState.value)
            }
    ) {
        content()
    }
}

@Composable
fun AvatarGridItem(
    avatar: AvatarManager.AvatarInfo,
    isSelected: Boolean,
    index: Int,
    totalItems: Int,
    isExiting: Boolean,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "avatar_scale"
    )

    StaggeredItem(
        index = index,
        totalItems = totalItems,
        isExiting = isExiting
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0x2200C2FF) else Color(0x0AFFFFFF))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        brush = if (isSelected) {
                            Brush.linearGradient(colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF)))
                        } else {
                            Brush.linearGradient(colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF)))
                        },
                        shape = CircleShape
                    )
                    .clickable { onSelect() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatar.resId),
                    contentDescription = avatar.displayName,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = avatar.displayName,
                color = if (isSelected) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LanguageOptionCard(
    name: String,
    available: Boolean,
    isSelected: Boolean,
    index: Int,
    totalItems: Int,
    isExiting: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "lang_scale"
    )

    StaggeredItem(
        index = index,
        totalItems = totalItems,
        isExiting = isExiting
    ) {
        GlassCard(
            cornerRadius = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    brush = if (isSelected) {
                        Brush.linearGradient(colors = listOf(Color(0xFF00C2FF), Color(0xFF00FFD2)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0x11FFFFFF), Color(0x11FFFFFF)))
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            onClick = { if (available) onClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0x2200FFD2) else if (available) Color(0x10FFFFFF) else Color(0x05FFFFFF)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!available) Icons.Default.Lock else if (isSelected) Icons.Default.Check else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF00FFD2) else if (available) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = name,
                    color = if (isSelected) Color(0xFF00FFD2) else if (available) Color.White else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                if (!available) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x11FF9D00))
                            .border(0.5.dp, Color(0x33FF9D00), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Soon",
                            color = Color(0xFFFFB300),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalOptionCard(
    goalStr: String,
    desc: String,
    isSelected: Boolean,
    index: Int,
    totalItems: Int,
    isExiting: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "goal_scale"
    )

    StaggeredItem(
        index = index,
        totalItems = totalItems,
        isExiting = isExiting
    ) {
        GlassCard(
            cornerRadius = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    brush = if (isSelected) {
                        Brush.linearGradient(colors = listOf(Color(0xFF00C2FF), Color(0xFF00FFD2)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0x11FFFFFF), Color(0x11FFFFFF)))
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = goalStr,
                    color = if (isSelected) Color(0xFF00FFD2) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingReminderView(
    enabled: Boolean,
    selectedTime: String,
    isExiting: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeSelect: (String, String) -> Unit
) {
    val hoursList = remember { (0..23).map { "%02d".format(it) } }
    val minutesList = remember { (0..59).map { "%02d".format(it) } }

    val initialHour = selectedTime.substringBefore(":", "08")
    val initialMinute = selectedTime.substringAfter(":", "00")

    val presets = listOf(
        "08:00" to "Morning",
        "12:00" to "Noon",
        "18:00" to "Sunset",
        "21:00" to "Prime",
        "23:00" to "Night"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StaggeredItem(index = 0, totalItems = 3, isExiting = isExiting) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF00C2FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (enabled) "Reminder Active ($selectedTime)" else "Reminder Disabled",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00C2FF),
                            checkedTrackColor = Color(0x3300C2FF)
                        )
                    )
                }
            }
        }

        if (enabled) {
            StaggeredItem(index = 1, totalItems = 3, isExiting = isExiting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfiniteWheelPicker(
                            items = hoursList,
                            selectedItem = initialHour,
                            onItemSelected = { onTimeSelect(it, initialMinute) }
                        )
                    }
                    Text(
                        text = ":",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        InfiniteWheelPicker(
                            items = minutesList,
                            selectedItem = initialMinute,
                            onItemSelected = { onTimeSelect(initialHour, it) }
                        )
                    }
                }
            }

            StaggeredItem(index = 2, totalItems = 3, isExiting = isExiting) {
                Column {
                    Text(
                        text = "Presets:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (time, label) ->
                            val active = selectedTime == time
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) Color(0x3300C2FF) else Color(0x0AFFFFFF))
                                    .border(
                                        width = 1.dp,
                                        color = if (active) Color(0xFF00C2FF) else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        val h = time.substringBefore(":")
                                        val m = time.substringAfter(":")
                                        onTimeSelect(h, m)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = time,
                                        color = if (active) Color(0xFF00FFD2) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = label,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsCount = items.size
    val repeatCount = 10000
    val totalItemsCount = itemsCount * repeatCount
    
    val centerIndex = (repeatCount / 2) * itemsCount
    val initialTargetIndex = centerIndex + items.indexOf(selectedItem).coerceAtLeast(0)
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialTargetIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val centerOffset = listState.layoutInfo.viewportSize.height / 2f
                val centerItem = visibleItems.minByOrNull { itemInfo ->
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    kotlin.math.abs(itemCenter - centerOffset)
                }
                if (centerItem != null) {
                    val realIndex = centerItem.index % itemsCount
                    onItemSelected(items[realIndex])
                    
                    // Force alignment centered inside the visual selector
                    val itemCenter = centerItem.offset + centerItem.size / 2f
                    val diff = itemCenter - centerOffset
                    if (kotlin.math.abs(diff) > 0.5f) {
                        listState.animateScrollBy(diff)
                    }
                }
            }
        }
    }
    
    LaunchedEffect(selectedItem) {
        if (!listState.isScrollInProgress) {
            val currentCenterItemIndex = listState.firstVisibleItemIndex
            val currentRealIndex = currentCenterItemIndex % itemsCount
            val targetRealIndex = items.indexOf(selectedItem).coerceAtLeast(0)
            if (currentRealIndex != targetRealIndex) {
                val baseIndex = (currentCenterItemIndex / itemsCount) * itemsCount
                listState.scrollToItem(baseIndex + targetRealIndex)
            }
        }
    }

    Box(
        modifier = modifier
            .height(110.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Color(0x1100C2FF))
                .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(8.dp))
        )
        
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 39.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalItemsCount) { index ->
                val realIndex = index % itemsCount
                val itemText = items[realIndex]
                
                val normalizedDistance = remember { derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    val itemInfo = visibleItems.firstOrNull { it.index == index }
                    if (itemInfo != null) {
                        val viewportCenter = layoutInfo.viewportSize.height / 2f
                        val currentItemCenter = itemInfo.offset + itemInfo.size / 2f
                        ((currentItemCenter - viewportCenter) / viewportCenter).coerceIn(-1f, 1f)
                    } else {
                        1f
                    }
                }}
                
                val dist = kotlin.math.abs(normalizedDistance.value)
                val scale = 1.15f - (dist * 0.35f)
                val alpha = 1f - (dist * 0.7f)
                val rotationX = normalizedDistance.value * -45f
                
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha.coerceIn(0.1f, 1f)
                            this.rotationX = rotationX
                            this.cameraDistance = 8f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemText,
                        color = if (dist < 0.15f) Color(0xFF00FFD2) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (dist < 0.15f) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF070814), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF070814))
                    )
                )
        )
    }
}

@Composable
fun OnboardingPlacementView(
    assessmentSession: AssessmentSession?,
    currentQuestion: AssessmentItem?,
    finished: Boolean,
    breakdown: String,
    isExiting: Boolean,
    onStart: () -> Unit,
    onAnswer: (Int) -> Unit,
    onSkip: () -> Unit,
    onProceed: () -> Unit
) {
    var selectedOptionIdx by remember { mutableStateOf(-1) }
    var isQuestionExiting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentQuestion) {
        selectedOptionIdx = -1
        isQuestionExiting = false
    }

    fun handleOptionClick(idx: Int) {
        if (selectedOptionIdx != -1 || isQuestionExiting) return
        selectedOptionIdx = idx
        coroutineScope.launch {
            delay(380) // Selection glow animation feedback
            isQuestionExiting = true
            delay(320) // Let current question and options staggered exit left
            onAnswer(idx)
        }
    }

    if (assessmentSession == null) {
        StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Vocabulary Placement Assessment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Take a smart 10-question adaptive vocabulary test to start from your matching CEFR level (A1-C2) directly.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    PremiumGlassButton(
                        text = "Start Smart Assessment",
                        onClick = onStart,
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Skip (Start from Beginner A1)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    } else if (!finished) {
        val totalItems = 6
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StaggeredItem(index = 0, totalItems = totalItems, isExiting = isExiting || isQuestionExiting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${assessmentSession.questionNumber} of 10",
                        color = Color(0xFF00FFD2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            StaggeredItem(index = 1, totalItems = totalItems, isExiting = isExiting || isQuestionExiting) {
                LinearProgressIndicator(
                    progress = { assessmentSession.progressPercent / 100f },
                    color = Color(0xFF00C2FF),
                    trackColor = Color(0x22FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            
            currentQuestion?.let { item ->
                StaggeredItem(index = 2, totalItems = totalItems, isExiting = isExiting || isQuestionExiting) {
                    Text(
                        text = item.question,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                item.options.forEachIndexed { idx, option ->
                    val isSelected = selectedOptionIdx == idx
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "option_scale"
                    )

                    StaggeredItem(index = 3 + idx, totalItems = totalItems, isExiting = isExiting || isQuestionExiting) {
                        GlassCard(
                            cornerRadius = 14.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    brush = if (isSelected) {
                                        Brush.linearGradient(colors = listOf(Color(0xFF00FFD2), Color(0xFF00C2FF)))
                                    } else {
                                        Brush.linearGradient(colors = listOf(Color(0x11FFFFFF), Color(0x11FFFFFF)))
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            onClick = { handleOptionClick(idx) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0x1A00FFD2) else Color.Transparent)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val prefix = when (idx) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF00FFD2) else Color(0x1AFFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prefix,
                                        color = if (isSelected) Color(0xFF070814) else Color(0xFF00C2FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option,
                                    color = if (isSelected) Color(0xFF00FFD2) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Finished", tint = Color(0xFF34D399), modifier = Modifier.size(40.dp))
                    Text(
                        text = "Assessment Finished!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = breakdown.substringAfterLast("Detected Level: "),
                        color = Color(0xFF00FFD2),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your adaptive vocabulary level was successfully mapped to local database indices.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PremiumGlassButton(
                        text = "Proceed to Next Step",
                        onClick = onProceed,
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingSetupView(
    setupState: SetupStep,
    isExiting: Boolean,
    onRetry: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (setupState) {
            is SetupStep.Idle -> {
                StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
                    WaveText("Preparing your learning journey...")
                }
            }
            is SetupStep.Downloading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StaggeredItem(index = 0, totalItems = 3, isExiting = isExiting) {
                        WaveText("Setting up SevenTicks...")
                    }
                    StaggeredItem(index = 1, totalItems = 3, isExiting = isExiting) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                SevenTicksLoader()
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { setupState.progress },
                                    color = Color(0xFF00C2FF),
                                    trackColor = Color(0x22FFFFFF),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
            SetupStep.Validating, SetupStep.Indexing, SetupStep.InitializingUserDb, SetupStep.PreparingLearnEngine, SetupStep.Finalizing -> {
                val label = when (setupState) {
                    SetupStep.Validating -> "Getting everything ready..."
                    SetupStep.Indexing -> "Preparing search indices..."
                    SetupStep.InitializingUserDb -> "Initializing profile indices..."
                    SetupStep.PreparingLearnEngine -> "Preparing Smart Learn..."
                    else -> "Finishing details..."
                }
                StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
                    WaveText(label)
                }
            }
            is SetupStep.Success -> {
                StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Database Setup Complete!",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            PremiumGlassButton(
                                text = "Start Learning Now",
                                onClick = onComplete,
                                icon = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            is SetupStep.Failure -> {
                StaggeredItem(index = 0, totalItems = 1, isExiting = isExiting) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Setup Failed",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = setupState.error,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            PremiumGlassButton(
                                text = "Retry Download",
                                onClick = onRetry,
                                icon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaveText(text: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_text")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_time"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            val yOffset = sin(time + index * 0.3f) * 5f
            Text(
                text = char.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .padding(horizontal = 0.5.dp)
            )
        }
    }
}

@Composable
fun SevenTicksLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "seven_ticks")
    val activeIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 7,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick_index"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 7) {
            val isActive = i == activeIndex
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.3f else 1.0f,
                animationSpec = tween(150),
                label = "tick_scale"
            )
            val color = if (isActive) Color(0xFF00FFD2) else Color(0x33FFFFFF)
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 1.dp,
                        color = if (isActive) Color(0xFF00C2FF) else Color.Transparent,
                        shape = CircleShape
                    )
            )
        }
    }
}
