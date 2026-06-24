package com.example.features.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder
import com.example.core.database.SetupStep
import com.example.core.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val repo = remember { SevenTicksApplication.instance.userRepository }

    var step by remember { mutableStateOf(0) }
    
    // User configuration states
    var userName by remember { mutableStateOf(prefs.userName) }
    var selectedAvatar by remember { mutableStateOf(prefs.avatar) }
    var nativeLanguage by remember { mutableStateOf(prefs.nativeLanguage) }
    var targetLanguage by remember { mutableStateOf(prefs.targetLanguage) }
    var dailyGoal by remember { mutableStateOf(prefs.dailyGoal) }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf(prefs.reminderTime) }
    var placementLevel by remember { mutableStateOf("A1 (Not Placed)") }

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
            prefs.userName = userName
            prefs.avatar = selectedAvatar
            prefs.nativeLanguage = nativeLanguage
            prefs.targetLanguage = targetLanguage
            prefs.dailyGoal = dailyGoal
            prefs.reminderTime = if (reminderEnabled) reminderTime else "Disabled"
            
            val mappedLevel = when (placementLevel) {
                "Beginner" -> 2   // A2
                "Intermediate" -> 3  // B1
                "Advanced" -> 4   // B2
                else -> 1        // A1
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
                        text = "Onboarding Wizard",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (step > 0 && step < 8) {
                        IconButton(onClick = { step-- }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
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

            // Central Animated Wizard Content Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "wizard_step"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> OnboardingWelcomeView()
                        1 -> OnboardingNameView(userName) { userName = it }
                        2 -> OnboardingAvatarView(selectedAvatar) { selectedAvatar = it }
                        3 -> OnboardingLanguageView("Your Native Language", nativeLanguage, listOf("Persian", "English", "Spanish", "German", "French", "Arabic", "Turkish", "Russian")) { nativeLanguage = it }
                        4 -> OnboardingLanguageView("Language to Learn", targetLanguage, listOf("English", "German", "French", "Spanish", "Japanese", "Chinese", "Italian", "Korean")) { targetLanguage = it }
                        5 -> OnboardingGoalView(dailyGoal) { dailyGoal = it }
                        6 -> OnboardingReminderView(reminderEnabled, reminderTime, onToggle = { reminderEnabled = it }, onTimeSelect = { reminderTime = it })
                        7 -> OnboardingPlacementView(
                            selectedLevel = placementLevel,
                            onSelectLevel = { placementLevel = it },
                            onSkip = {
                                placementLevel = "A1"
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PremiumGlassButton(
                        text = "Next Step",
                        onClick = {
                            // Save intermediate step state to prefs
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// 1. Welcome View
@Composable
fun OnboardingWelcomeView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TikiPlaceholder(message = "Hi there! I am Tiki, your vocabulary mentor!")
        Spacer(modifier = Modifier.height(24.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        Icon(
            Icons.Default.AccountBox,
            contentDescription = null,
            tint = Color(0xFF00C2FF),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What should we call you?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Your Name", color = Color.White.copy(alpha = 0.6f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x0AFFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedIndicatorColor = Color(0xFF00C2FF),
                    unfocusedIndicatorColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFF00C2FF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 3. Avatar View
@Composable
fun OnboardingAvatarView(selectedAvatar: String, onSelectAvatar: (String) -> Unit) {
    val avatars = listOf(
        "avatar_1" to Icons.Default.Face,
        "avatar_2" to Icons.Default.Person,
        "avatar_3" to Icons.Default.AccountCircle,
        "avatar_4" to Icons.Default.Star,
        "avatar_5" to Icons.Default.Favorite,
        "avatar_6" to Icons.Default.PlayArrow
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your avatar",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(240.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(avatars) { (id, iconVec) ->
                val active = selectedAvatar == id
                val borderGradient = if (active) {
                    Brush.linearGradient(colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF)))
                } else {
                    Brush.linearGradient(colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF)))
                }
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (active) Color(0x2200C2FF) else Color(0x0AFFFFFF))
                        .border(width = 2.dp, brush = borderGradient, shape = CircleShape)
                        .clickable { onSelectAvatar(id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVec,
                        contentDescription = null,
                        tint = if (active) Color(0xFF00FFD2) else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

// 4 & 5. Languages View (Searchable)
@Composable
fun OnboardingLanguageView(
    title: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptions = options.filter { it.contains(searchQuery, ignoreCase = true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Language...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x0AFFFFFF),
                unfocusedContainerColor = Color(0x0AFFFFFF),
                focusedIndicatorColor = Color(0xFF00C2FF),
                unfocusedIndicatorColor = Color(0x22FFFFFF),
                cursorColor = Color(0xFF00C2FF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredOptions) { lang ->
                val active = selected == lang
                GlassCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(lang) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            color = if (active) Color(0xFF00FFD2) else Color.White,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        if (active) {
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

// 6. Goal View (Choices: 5, 10, 15, 20, 30 minutes)
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
        Text(
            text = "Select your daily goal",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
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

// 7. Reminder View
@Composable
fun OnboardingReminderView(
    enabled: Boolean,
    selectedTime: String,
    onToggle: (Boolean) -> Unit,
    onTimeSelect: (String) -> Unit
) {
    val standardTimes = listOf("08:00 AM", "09:00 AM", "12:00 PM", "06:00 PM", "08:00 PM", "10:00 PM")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF9D00FF),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Set daily review reminder",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Reviewing vocabulary consistently is key to spaced repetition success.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        
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
            Text(
                text = "Select time:",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(standardTimes) { time ->
                    val active = selectedTime == time
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) Color(0x3300C2FF) else Color(0x0AFFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (active) Color(0xFF00C2FF) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onTimeSelect(time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            color = if (active) Color(0xFF00FFD2) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// 8. Placement Test View (Optional)
@Composable
fun OnboardingPlacementView(
    selectedLevel: String,
    onSelectLevel: (String) -> Unit,
    onSkip: () -> Unit
) {
    val levels = listOf("Beginner", "Intermediate", "Advanced")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Placement Test",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSkip) {
                Text("Skip (Start A1)", color = Color(0xFF00FFD2), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Solve this query to determine your initial list profile placement level:\nWhat is the best definition of 'Eloquent'?",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectLevel("Intermediate") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLevel == "Intermediate",
                        onClick = { onSelectLevel("Intermediate") },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00FFD2))
                    )
                    Text("A) Fluent or persuasive in speaking/writing.", color = Color.White, fontSize = 14.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectLevel("Beginner") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLevel == "Beginner",
                        onClick = { onSelectLevel("Beginner") },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00FFD2))
                    )
                    Text("B) Easily broken or fragile.", color = Color.White, fontSize = 14.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectLevel("Advanced") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLevel == "Advanced",
                        onClick = { onSelectLevel("Advanced") },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00FFD2))
                    )
                    Text("C) Showing fear or lack of confidence.", color = Color.White, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Selected Placement Level: $selectedLevel",
            color = Color(0xFF00C2FF),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// 9. Setup Progress & Failure Handling View
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
                TikiPlaceholder(message = "Preparing local environment configurations...")
            }
            is SetupStep.Downloading -> {
                TikiPlaceholder(message = "Downloading vocabulary database, please hold on!")
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Downloading Vocabulary Database",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${(setupState.progress * 100).toInt()}% downloaded",
                            color = Color(0xFF00FFD2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            SetupStep.Validating -> {
                TikiPlaceholder(message = "Validating downloaded vocabulary index files...")
                Spacer(modifier = Modifier.height(24.dp))
                SetupStepItem("Validating Database...", active = true)
            }
            SetupStep.Indexing -> {
                TikiPlaceholder(message = "Caching search indexes for blazing fast search...")
                Spacer(modifier = Modifier.height(24.dp))
                SetupStepItem("Preparing Search Index...", active = true)
            }
            SetupStep.InitializingUserDb -> {
                TikiPlaceholder(message = "Creating secure local database for your profile...")
                Spacer(modifier = Modifier.height(24.dp))
                SetupStepItem("Creating User Database...", active = true)
            }
            SetupStep.PreparingLearnEngine -> {
                TikiPlaceholder(message = "Optimizing Leitner and SuperMemo learning boxes...")
                Spacer(modifier = Modifier.height(24.dp))
                SetupStepItem("Preparing Smart Learn...", active = true)
            }
            SetupStep.Finalizing -> {
                TikiPlaceholder(message = "Just finishing up details. Almost done!")
                Spacer(modifier = Modifier.height(24.dp))
                SetupStepItem("Finalizing Setup...", active = true)
            }
            is SetupStep.Success -> {
                TikiPlaceholder(message = "Woohoo! Your local environment setup is perfect. Let's begin our mastery journey!")
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                TikiPlaceholder(message = "Oh no, there was an error in downloading the database. Let's retry.")
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
fun SetupStepItem(label: String, active: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (active) {
                CircularProgressIndicator(
                    color = Color(0xFF00C2FF),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
