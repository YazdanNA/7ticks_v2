package com.example.features.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.core.assessment.CEFRLevel
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

    var step by remember { mutableStateOf(0) }
    
    // User configuration states
    var userName by remember { mutableStateOf("") } // Starts empty, no pre-filled values
    var selectedAvatar by remember { mutableStateOf("") }
    var nativeLanguage by remember { mutableStateOf("Persian") }
    var targetLanguage by remember { mutableStateOf("English") }
    var dailyGoal by remember { mutableStateOf("10 minutes / day") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00") } // 24h format default
    var placementLevel by remember { mutableStateOf("A1") }

    // Adaptive assessment states
    var assessmentSession by remember { mutableStateOf<AssessmentSession?>(null) }
    var currentQuestion by remember { mutableStateOf<AssessmentItem?>(null) }
    var assessmentResultBreakdown by remember { mutableStateOf("") }
    var assessmentFinished by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var assessmentTikiState by remember { mutableStateOf("st-poker") }

    // Initialize first avatar from dynamic bank if empty
    LaunchedEffect(Unit) {
        val avatars = AvatarManager.getAvailableAvatars(context)
        if (avatars.isNotEmpty() && selectedAvatar.isEmpty()) {
            selectedAvatar = avatars.first().id
        }
    }

    // Environment setup state
    var setupState by remember { mutableStateOf<SetupStep>(SetupStep.Idle) }

    val totalSteps = 9
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
            // Save final selections to prefs before setup starts
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

            // Trigger actual setup
            repo.runEnvironmentSetup().collectLatest { state ->
                setupState = state
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Setup Your 7Ticks",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {}, // Top back arrow completely removed
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Steps Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until totalSteps) {
                    val active = i <= step
                    val color = if (active) Color(0xFF00C2FF) else Color(0x33FFFFFF)
                    val width = if (i == step) 24.dp else 8.dp
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(width)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }

            // Central Fixed Content Box with vertical transition (no horizontal slides)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { 24 } togetherWith
                        fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { -12 }
                    },
                    label = "wizard_step"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> OnboardingWelcomeView()
                        1 -> OnboardingNameView(userName) { userName = it }
                        2 -> OnboardingAvatarView(selectedAvatar) { selectedAvatar = it }
                        3 -> OnboardingNativeLanguageView(nativeLanguage) { nativeLanguage = it }
                        4 -> OnboardingTargetLanguageView(targetLanguage) { targetLanguage = it }
                        5 -> OnboardingGoalView(dailyGoal) { dailyGoal = it }
                        6 -> OnboardingReminderView(reminderEnabled, reminderTime, onToggle = { reminderEnabled = it }) { h, m -> reminderTime = "$h:$m" }
                        7 -> OnboardingPlacementView(
                            assessmentSession = assessmentSession,
                            currentQuestion = currentQuestion,
                            finished = assessmentFinished,
                            breakdown = assessmentResultBreakdown,
                            tikiState = assessmentTikiState,
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
                                    
                                    coroutineScope.launch {
                                        delay(1200)
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
                                            assessmentTikiState = "st-poker"
                                        }
                                        lastAnswerCorrect = null
                                    }
                                }
                            },
                            onSkip = {
                                placementLevel = "A1"
                                step++
                            },
                            onProceed = {
                                step++
                            }
                        )
                        8 -> OnboardingSetupView(
                            setupState = setupState,
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

            // Bottom Navigation Controllers
            if (step < 8) {
                Spacer(modifier = Modifier.height(16.dp))
                if (step == 0) {
                    // First step: Big Centered Continue Button
                    PremiumGlassButton(
                        text = "Continue",
                        onClick = { step++ },
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
                    // Placement is self-controlled, bottom controller is hidden or adapted
                } else {
                    // All other pages: Primary Next on right, Small secondary Back on left
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        OutlinedButton(
                            onClick = { step-- },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(0.4f)
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontWeight = FontWeight.Bold)
                        }

                        // Next Button
                        val isNameValid = step != 1 || userName.trim().isNotEmpty()
                        PremiumGlassButton(
                            text = "Next",
                            enabled = isNameValid,
                            onClick = {
                                when (step) {
                                    1 -> prefs.userName = userName
                                    2 -> prefs.avatar = selectedAvatar
                                    3 -> prefs.nativeLanguage = nativeLanguage
                                    4 -> prefs.targetLanguage = targetLanguage
                                    5 -> prefs.dailyGoal = dailyGoal
                                    6 -> prefs.reminderTime = if (reminderEnabled) reminderTime else "Disabled"
                                }
                                step++
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

// ── Step Views ──────────────────────────────────────────────────────────

// 1. Welcome View
@Composable
fun OnboardingWelcomeView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(
            tikiState = "st-welcome",
            message = "Hi there! I am Tiki, your vocabulary mentor!"
        )
        Spacer(modifier = Modifier.height(24.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Welcome to 7Ticks",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A scientifically proven spaced repetition engine designed to help you remember vocabularies permanently in just 7 ticks.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// 2. Name View
@Composable
fun OnboardingNameView(name: String, onNameChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(
            tikiState = "st-name",
            message = "Let's get to know you! What is your name?"
        )
        Spacer(modifier = Modifier.height(24.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("Enter your name", color = Color.White.copy(alpha = 0.4f)) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x0AFFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF),
                        focusedIndicatorColor = Color(0xFF00C2FF),
                        unfocusedIndicatorColor = Color(0x33FFFFFF),
                        cursorColor = Color(0xFF00C2FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 3. Avatar View
@Composable
fun OnboardingAvatarView(selectedAvatar: String, onSelectAvatar: (String) -> Unit) {
    val context = LocalContext.current
    val avatars = remember { AvatarManager.getAvailableAvatars(context) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(
            tikiState = "st-happy",
            message = "Choose an avatar that fits your style!"
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(220.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(avatars) { avatar ->
                val active = selectedAvatar == avatar.id
                val scale by animateFloatAsState(
                    targetValue = if (active) 1.15f else 1.0f,
                    animationSpec = tween(200, easing = EaseInOutSine),
                    label = "avatar_scale"
                )
                val borderGradient = if (active) {
                    Brush.linearGradient(colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF)))
                } else {
                    Brush.linearGradient(colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF)))
                }
                
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (active) Color(0x2200C2FF) else Color(0x0AFFFFFF))
                        .border(width = 2.dp, brush = borderGradient, shape = CircleShape)
                        .clickable { onSelectAvatar(avatar.id) },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = avatar.resId),
                        contentDescription = avatar.displayName,
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                }
            }
        }
    }
}

// 4. Native Language Selection (No search bar, Coming Soon badges)
@Composable
fun OnboardingNativeLanguageView(
    selected: String,
    onSelect: (String) -> Unit
) {
    val languages = listOf(
        "Persian" to true,
        "English" to false,
        "German" to false,
        "French" to false
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TikiPlaceholder(
            tikiState = "st-native-lang",
            message = "Select your native language so I can customize the translations."
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.height(250.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(languages.size) { idx ->
                val (lang, available) = languages[idx]
                val active = selected == lang && available
                
                GlassCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (available) onSelect(lang) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            color = if (active) Color(0xFF00FFD2) else if (available) Color.White else Color.White.copy(alpha = 0.4f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        if (!available) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33FF9D00))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Coming Soon",
                                    color = Color(0xFFFFB300),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (active) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00FFD2)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. Target Language Selection (No search bar, Coming Soon badges)
@Composable
fun OnboardingTargetLanguageView(
    selected: String,
    onSelect: (String) -> Unit
) {
    val languages = listOf(
        "English" to true,
        "German" to false,
        "French" to false,
        "Spanish" to false,
        "Japanese" to false
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TikiPlaceholder(
            tikiState = "st-target-lang",
            message = "Select the target language you want to master!"
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.height(280.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(languages.size) { idx ->
                val (lang, available) = languages[idx]
                val active = selected == lang && available
                
                GlassCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (available) onSelect(lang) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            color = if (active) Color(0xFF00FFD2) else if (available) Color.White else Color.White.copy(alpha = 0.4f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        if (!available) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33FF9D00))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Coming Soon",
                                    color = Color(0xFFFFB300),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (active) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00FFD2)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. Goal View
@Composable
fun OnboardingGoalView(selectedGoal: String, onSelectGoal: (String) -> Unit) {
    val goals = listOf(
        "5 minutes / day" to "Casual Learning Session",
        "10 minutes / day" to "Regular Study Practice",
        "15 minutes / day" to "Serious Vocabulary Commitment",
        "20 minutes / day" to "Intense Spaced Practice",
        "30 minutes / day" to "Insane Mastery Speedrun"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(
            tikiState = "st-study-time",
            message = "How much time would you like to dedicate to learning daily?"
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            goals.forEach { (goalStr, desc) ->
                val active = selectedGoal == goalStr
                GlassCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectGoal(goalStr) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                  text = goalStr,
                                  color = if (active) Color(0xFF00FFD2) else Color.White,
                                  fontWeight = FontWeight.Bold,
                                  fontSize = 16.sp
                            )
                            Text(
                                  text = desc,
                                  color = Color.White.copy(alpha = 0.5f),
                                  fontSize = 12.sp
                            )
                        }
                        if (active) {
                            Icon(
                                  Icons.Default.Star,
                                  contentDescription = null,
                                  tint = Color(0xFFFFFFD0)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. Reminder View (iPhone Wheel Picker + Smooth Preset Selection)
@Composable
fun OnboardingReminderView(
    enabled: Boolean,
    selectedTime: String, // format "HH:MM"
    onToggle: (Boolean) -> Unit,
    onTimeSelect: (String, String) -> Unit
) {
    val hoursList = remember { (0..23).map { "%02d".format(it) } }
    val minutesList = remember { (0..59).map { "%02d".format(it) } }

    val initialHour = selectedTime.substringBefore(":", "08")
    val initialMinute = selectedTime.substringAfter(":", "00")

    val presets = listOf(
        "08:00" to "Morning Review",
        "12:00" to "Noon Review",
        "18:00" to "Sunset Review",
        "20:00" to "Prime Review",
        "22:00" to "Night Review"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(
            tikiState = "st-remind-time",
            message = "Set a daily reminder time so we don't break your streak!"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF00C2FF)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (enabled) "Reminder Active ($selectedTime)" else "Reminder Disabled",
                        color = Color.White,
                        fontSize = 15.sp,
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

        if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Picker
                Box(modifier = Modifier.weight(1f)) {
                    WheelPicker(
                        items = hoursList,
                        selectedItem = initialHour,
                        onItemSelected = { onTimeSelect(it, initialMinute) }
                    )
                }
                Text(":", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                // Minute Picker
                Box(modifier = Modifier.weight(1f)) {
                    WheelPicker(
                        items = minutesList,
                        selectedItem = initialMinute,
                        onItemSelected = { onTimeSelect(initialHour, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Presets:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (time, title) ->
                    val active = selectedTime == time
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) Color(0x3300C2FF) else Color(0x0AFFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (active) Color(0xFF00C2FF) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                val h = time.substringBefore(":")
                                val m = time.substringAfter(":")
                                onTimeSelect(h, m)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(time, color = if (active) Color(0xFF00FFD2) else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(title.substringBefore(" "), color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

// iPhone Wheel Picker Composable
@Composable
fun WheelPicker(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val targetIndex = items.indexOf(selectedItem)
    if (targetIndex != -1) {
        LaunchedEffect(selectedItem) {
            if (listState.firstVisibleItemIndex != targetIndex && !listState.isScrollInProgress) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
    
    // Detect scrolling selection
    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIdx = listState.firstVisibleItemIndex
            if (centerIdx in items.indices) {
                onItemSelected(items[centerIdx])
            }
        }
    }

    Box(
        modifier = modifier.height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection glass overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(0x1100C2FF))
                .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(8.dp))
        )
        
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 57.dp), // centers item mathematically
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { idx, item ->
                val isSelected = item == selectedItem
                val scale = if (isSelected) 1.2f else 0.8f
                val alpha = if (isSelected) 1f else 0.4f
                
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) Color(0xFF00FFD2) else Color.White,
                        fontSize = 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .scale(scale)
                            .graphicsLayer(alpha = alpha)
                    )
                }
            }
        }
    }
}

// 8. Adaptive Placement Assessment View
@Composable
fun OnboardingPlacementView(
    assessmentSession: AssessmentSession?,
    currentQuestion: AssessmentItem?,
    finished: Boolean,
    breakdown: String,
    tikiState: String,
    onStart: () -> Unit,
    onAnswer: (Int) -> Unit,
    onSkip: () -> Unit,
    onProceed: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (assessmentSession == null) {
            // Initial option selector
            TikiPlaceholder(
                tikiState = "st-placement",
                message = "Let's determine your vocabulary placement level!"
            )
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Vocabulary Placement Assessment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Take a smart 10-question adaptive vocabulary test to start from your matching CEFR level (A1-C2) directly.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    PremiumGlassButton(
                        text = "Start Smart Assessment",
                        onClick = onStart,
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Skip (Start from Beginner A1)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!finished) {
            // Question playing
            TikiPlaceholder(
                tikiState = tikiState,
                message = "I'm assessing your skill with every answer. Focus up!"
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Question ${assessmentSession.questionNumber} of 10",
                color = Color(0xFF00FFD2),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { assessmentSession.progressPercent / 100f },
                color = Color(0xFF00C2FF),
                trackColor = Color(0x22FFFFFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            currentQuestion?.let { item ->
                Text(
                    text = item.question,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item.options.forEachIndexed { idx, option ->
                        GlassCard(
                            cornerRadius = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onAnswer(idx) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1AFFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(prefix, color = Color(0xFF00C2FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(option, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Completed summary results page
            TikiPlaceholder(
                tikiState = "st-welcome",
                message = "Congratulations! Your profile setup is perfectly configured."
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Finished", tint = Color(0xFF34D399), modifier = Modifier.size(48.dp))
                    Text(
                        "Assessment Finished!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        breakdown.substringAfterLast("Detected Level: "),
                        color = Color(0xFF00FFD2),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Your adaptive vocabulary level was successfully mapped to local database indices.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PremiumGlassButton(
                        text = "Proceed to Next Step",
                        onClick = onProceed,
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// 9. Setup Progress View
@Composable
fun OnboardingSetupView(
    setupState: SetupStep,
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
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Preparing your learning journey...")
            }
            is SetupStep.Downloading -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(24.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        SevenTicksLoader()
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { setupState.progress },
                            color = Color(0xFF00C2FF),
                            trackColor = Color(0x22FFFFFF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            SetupStep.Validating -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Getting everything ready...")
            }
            SetupStep.Indexing -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Preparing search indices...")
            }
            SetupStep.InitializingUserDb -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Initializing profile indices...")
            }
            SetupStep.PreparingLearnEngine -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Preparing Smart Learn...")
            }
            SetupStep.Finalizing -> {
                TikiPlaceholder(tikiState = "st-loading-data", message = "Setting up SevenTicks...")
                Spacer(modifier = Modifier.height(16.dp))
                WaveText("Finishing details...")
            }
            is SetupStep.Success -> {
                TikiPlaceholder(tikiState = "st-welcome", message = "Woohoo! Your local environment setup is perfect. Let's begin our mastery journey!")
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Database Setup Complete!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        PremiumGlassButton(
                            text = "Start Learning Now",
                            onClick = onComplete,
                            icon = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            is SetupStep.Failure -> {
                TikiPlaceholder(tikiState = "st-sad", message = "Oh no, there was an error in downloading the database. Let's retry.")
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Setup Failed",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = setupState.error,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        PremiumGlassButton(
                            text = "Retry Download",
                            onClick = onRetry,
                            icon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Staggered Sine Wave text animation
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
            val yOffset = sin(time + index * 0.3f) * 6f
            Text(
                text = char.toString(),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .offset(y = yOffset.dp)
                    .padding(horizontal = 1.dp)
            )
        }
    }
}

// Seven pulsating loader circles
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 7) {
            val isActive = i == activeIndex
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.35f else 1.0f,
                animationSpec = tween(150),
                label = "tick_scale"
            )
            val color = if (isActive) Color(0xFF00FFD2) else Color(0x33FFFFFF)
            
            Box(
                modifier = Modifier
                    .size(12.dp)
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
