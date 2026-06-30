package com.example.features.boxes.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.ui.components.TickyCard
import com.example.core.ui.components.SevenTicksFAB
import com.example.core.ui.components.UniversalFlashcard
import com.example.core.ui.components.toFlashcardData
import com.example.core.ui.components.flashcard.FlashCardState
import com.example.core.ui.components.SharedTextField
import com.example.core.learning.*
import com.example.core.fsrs.ReviewRatingModel
import com.example.core.database.BoxWordEntity
import com.example.core.database.toWordDetails
import com.example.core.database.CustomBoxEntity
import com.example.core.database.SearchResult
import com.example.core.database.ReviewHistoryEntity
import androidx.compose.animation.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Local Navigation states for Custom Boxes sub-flow
sealed class BoxesSubScreen {
    object Dashboard : BoxesSubScreen()
    data class CreateEditBox(val boxId: Int? = null) : BoxesSubScreen()
    data class BoxDetail(val boxId: Int) : BoxesSubScreen()
    data class AddWord(val boxId: Int, val boxWordId: Int? = null) : BoxesSubScreen()
    data class WordDetail(val boxId: Int, val boxWordId: Int) : BoxesSubScreen()
    data class BoxStudy(val boxId: Int) : BoxesSubScreen()
    object ImportBackup : BoxesSubScreen()
}

private fun serializeSubScreen(screen: BoxesSubScreen): String {
    return when (screen) {
        is BoxesSubScreen.Dashboard -> "Dashboard"
        is BoxesSubScreen.CreateEditBox -> "CreateEditBox:${screen.boxId ?: "null"}"
        is BoxesSubScreen.BoxDetail -> "BoxDetail:${screen.boxId}"
        is BoxesSubScreen.AddWord -> "AddWord:${screen.boxId}:${screen.boxWordId ?: "null"}"
        is BoxesSubScreen.WordDetail -> "WordDetail:${screen.boxId}:${screen.boxWordId}"
        is BoxesSubScreen.BoxStudy -> "BoxStudy:${screen.boxId}"
        is BoxesSubScreen.ImportBackup -> "ImportBackup"
    }
}

private fun deserializeSubScreen(str: String): BoxesSubScreen {
    val parts = str.split(":")
    if (parts.isEmpty()) return BoxesSubScreen.Dashboard
    return when (parts[0]) {
        "Dashboard" -> BoxesSubScreen.Dashboard
        "CreateEditBox" -> {
            val idStr = parts.getOrNull(1) ?: "null"
            BoxesSubScreen.CreateEditBox(if (idStr == "null") null else idStr.toIntOrNull())
        }
        "BoxDetail" -> {
            val id = parts.getOrNull(1)?.toIntOrNull() ?: 0
            BoxesSubScreen.BoxDetail(id)
        }
        "AddWord" -> {
            val boxId = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val wordIdStr = parts.getOrNull(2) ?: "null"
            BoxesSubScreen.AddWord(boxId, if (wordIdStr == "null") null else wordIdStr.toIntOrNull())
        }
        "WordDetail" -> {
            val boxId = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val wordId = parts.getOrNull(2)?.toIntOrNull() ?: 0
            BoxesSubScreen.WordDetail(boxId, wordId)
        }
        "BoxStudy" -> {
            val id = parts.getOrNull(1)?.toIntOrNull() ?: 0
            BoxesSubScreen.BoxStudy(id)
        }
        "ImportBackup" -> BoxesSubScreen.ImportBackup
        else -> BoxesSubScreen.Dashboard
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoxesScreen(
    navController: androidx.navigation.NavController? = null,
    onShowBottomBar: (Boolean) -> Unit = {}
) {
    var currentSubScreenStr by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("Dashboard") }
    var backstackStr by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var isBackNavigation by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val currentSubScreen = remember(currentSubScreenStr) { deserializeSubScreen(currentSubScreenStr) }

    LaunchedEffect(currentSubScreenStr) {
        onShowBottomBar(deserializeSubScreen(currentSubScreenStr) is BoxesSubScreen.Dashboard)
    }

    LaunchedEffect(Unit) {
        com.example.features.tiki.api.TikiController.getInstance().triggerPipeline(
            contextEvent = com.example.features.tiki.context.ContextEvent.Custom("Idle"),
            behaviorEvent = com.example.features.tiki.behavior.BehaviorEvent.BoxesOpened
        )
    }

    fun navigateTo(screen: BoxesSubScreen) {
        isBackNavigation = false
        val newBackstack = if (backstackStr.isEmpty()) currentSubScreenStr else "$backstackStr;$currentSubScreenStr"
        backstackStr = newBackstack
        currentSubScreenStr = serializeSubScreen(screen)
    }

    fun navigateBack() {
        isBackNavigation = true
        if (backstackStr.isNotEmpty()) {
            val list = backstackStr.split(";")
            val lastIndex = list.size - 1
            val previousScreenStr = list[lastIndex]
            currentSubScreenStr = previousScreenStr
            backstackStr = list.take(lastIndex).joinToString(";")
        } else {
            currentSubScreenStr = "Dashboard"
        }
    }

    // Intercept back clicks when not on the main dashboard of Boxes screen
    val canGoBack = currentSubScreen !is BoxesSubScreen.Dashboard || backstackStr.isNotEmpty()
    BackHandler(enabled = canGoBack) {
        navigateBack()
    }

    // Wrap in animated transition content with SharedTransitionLayout
    SharedTransitionLayout {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (isBackNavigation) {
                    (slideInHorizontally { width -> -width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                } else {
                    (slideInHorizontally { width -> width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                }
            },
            label = "boxes_subscreen_switch"
        ) { screen ->
            when (screen) {
                is BoxesSubScreen.Dashboard -> {
                    BoxesDashboardScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onNavigateToCreateBox = { navigateTo(BoxesSubScreen.CreateEditBox()) },
                        onNavigateToEditBox = { id -> navigateTo(BoxesSubScreen.CreateEditBox(id)) },
                        onNavigateToBoxDetail = { id -> navigateTo(BoxesSubScreen.BoxDetail(id)) },
                        onNavigateToImport = { navigateTo(BoxesSubScreen.ImportBackup) }
                    )
                }
                is BoxesSubScreen.CreateEditBox -> {
                    CreateEditBoxScreen(
                        boxId = screen.boxId,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onBack = { navigateBack() }
                    )
                }
                is BoxesSubScreen.BoxDetail -> {
                    BoxDetailScreen(
                        boxId = screen.boxId,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onBack = { navigateBack() },
                        onNavigateToAddWord = { boxId -> navigateTo(BoxesSubScreen.AddWord(boxId)) },
                        onNavigateToEditWord = { boxId, wordId -> navigateTo(BoxesSubScreen.AddWord(boxId, wordId)) },
                        onNavigateToWordDetail = { boxId, wordId -> navigateTo(BoxesSubScreen.WordDetail(boxId, wordId)) },
                        onNavigateToStudy = { boxId -> navigateTo(BoxesSubScreen.BoxStudy(boxId)) }
                    )
                }
                is BoxesSubScreen.AddWord -> {
                    AddWordScreen(
                        boxId = screen.boxId,
                        boxWordId = screen.boxWordId,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onBack = { navigateBack() }
                    )
                }
            is BoxesSubScreen.WordDetail -> {
                BoxWordDetailScreen(
                    boxId = screen.boxId,
                    boxWordId = screen.boxWordId,
                    onBack = { navigateBack() }
                )
            }
            is BoxesSubScreen.BoxStudy -> {
                LaunchedEffect(screen.boxId) {
                    val route = com.example.core.navigation.Screen.LearningSession.createRoute(isBoxSession = true, boxId = screen.boxId)
                    navController?.navigate(route)
                    navigateBack()
                }
                Box(modifier = Modifier.fillMaxSize())
            }
            is BoxesSubScreen.ImportBackup -> {
                ImportBackupScreen(
                    onBack = { navigateBack() }
                )
            }
        }
    }
}
}

// Icon dictionary mapper
fun getBoxIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "Star" -> Icons.Default.Star
        "Book" -> Icons.Default.Book
        "Info" -> Icons.Default.Info
        "Favorite" -> Icons.Default.Favorite
        "Home" -> Icons.Default.Home
        "Face" -> Icons.Default.Face
        "Lock" -> Icons.Default.Lock
        "Lightbulb" -> Icons.Default.Info
        else -> Icons.Default.Folder
    }
}

// Format Unix Timestamp
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Available Icon names
val ICON_LIST = listOf("Folder", "Star", "Book", "Favorite", "Home", "Face", "Lock", "Lightbulb")

// Beautiful Premium Colors for Picker
val COLOR_PICKERS = listOf(
    "#00C2FF", // Cyan
    "#9D00FF", // Purple
    "#FFD600", // Yellow
    "#00E676", // Green
    "#FF4081", // Pink
    "#FF7043", // Orange
    "#00FFD2", // Mint
    "#FFFFFF"  // White
)

// ====================================================
// 1. DASHBOARD SCREEN
// ====================================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BoxesDashboardScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToCreateBox: () -> Unit,
    onNavigateToEditBox: (Int) -> Unit,
    onNavigateToBoxDetail: (Int) -> Unit,
    onNavigateToImport: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val allBoxes by boxRepo.getCustomBoxes().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showArchivedOnly by remember { mutableStateOf(false) }

    // Map each box to its words to calculate counts dynamically
    val boxWordCounts = remember { mutableStateMapOf<Int, Int>() }
    val boxMasteredCounts = remember { mutableStateMapOf<Int, Int>() }
    val boxDueCounts = remember { mutableStateMapOf<Int, Int>() }

    // Collect words stats for each box dynamically
    allBoxes.forEach { box ->
        val wordsFlow = remember(box.id) { boxRepo.getWordsInCustomBox(box.id) }
        val wordsList by wordsFlow.collectAsState(initial = emptyList())
        LaunchedEffect(wordsList) {
            boxWordCounts[box.id] = wordsList.size
            // Leitner box 7 is "Mastered"
            boxMasteredCounts[box.id] = wordsList.count { it.boxIndex == 7 }
            // FSRS due date check
            boxDueCounts[box.id] = wordsList.count { it.dueDate <= System.currentTimeMillis() }
        }
    }

    val filteredBoxes = allBoxes.filter { box ->
        val matchesSearch = box.name.contains(searchQuery, ignoreCase = true) || box.description.contains(searchQuery, ignoreCase = true)
        val matchesArchive = box.isArchived == showArchivedOnly
        matchesSearch && matchesArchive
    }

    val scrollState = rememberScrollState()

    // Smart FAB Scrolling direction logic
    var previousScrollOffset by remember { mutableStateOf(0) }
    var isFabExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.value) {
        val delta = scrollState.value - previousScrollOffset
        if (delta > 10 && scrollState.isScrollInProgress) {
            isFabExpanded = false // scrolling down -> collapse
        } else if (delta < -10 && scrollState.isScrollInProgress) {
            isFabExpanded = true  // scrolling up -> expand
        }
        previousScrollOffset = scrollState.value
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dashboard Title (FIXED)
            Column {
                Text(
                    text = "My Vocab Boxes",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Personal study collections",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            // Search Bar (FIXED)
            SharedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search Boxes",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Glassmorphic List or Empty State
                if (filteredBoxes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TickyCard(
                    message = if (showArchivedOnly) "You don't have any archived boxes yet." else "Let's create your first custom vocabulary collection! Tap 'Create Box' above.",
                    sizeDp = 64,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            filteredBoxes.forEach { box ->
                val wordCount = boxWordCounts[box.id] ?: 0
                val masteredCount = boxMasteredCounts[box.id] ?: 0
                val progressPercent = if (wordCount > 0) (masteredCount * 100) / wordCount else 0
                val boxColor = Color(android.graphics.Color.parseColor(box.colorHex))

                var showMenu by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    GlassCard(
                        modifier = with(sharedTransitionScope) {
                            Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = "box_card_${box.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .fillMaxWidth()
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToBoxDetail(box.id)
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row with Icon, Name, and Menu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(boxColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getBoxIcon(box.iconName),
                                            contentDescription = null,
                                            tint = boxColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = box.name,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (box.description.isNotEmpty()) {
                                            Text(
                                                text = box.description,
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            // Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    val dueCount = boxDueCounts[box.id] ?: 0
                                    Text(
                                        text = "$wordCount words total • $dueCount due • $masteredCount mastered",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Activity: ${formatDateTime(box.lastActivityAt)}",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 9.sp
                                    )
                                }
                                Text(
                                    text = "$progressPercent%",
                                    color = boxColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Progress Indicator
                            LinearProgressIndicator(
                                progress = { if (wordCount > 0) masteredCount.toFloat() / wordCount.toFloat() else 0f },
                                color = boxColor,
                                trackColor = Color(0x11FFFFFF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    // Context dropdown menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF0F1026))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit / Rename", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                            onClick = {
                                showMenu = false
                                onNavigateToEditBox(box.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (box.isArchived) "Unarchive" else "Archive", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    boxRepo.archiveCustomBox(box.id, !box.isArchived)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    boxRepo.duplicateCustomBox(box.id)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export JSON Backup", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val backupJson = withContext(Dispatchers.IO) {
                                        val words = boxRepo.getWordsInCustomBoxOnce(box.id)
                                        val obj = JSONObject()
                                        obj.put("version", 1)
                                        obj.put("name", box.name)
                                        obj.put("description", box.description)
                                        obj.put("iconName", box.iconName)
                                        obj.put("colorHex", box.colorHex)
                                        val arr = JSONArray()
                                        words.forEach { w ->
                                            val wObj = JSONObject()
                                            wObj.put("word", w.word)
                                            wObj.put("phoneticsUs", w.phoneticsUs ?: "")
                                            wObj.put("phoneticsUk", w.phoneticsUk ?: "")
                                            wObj.put("definitions", w.definitions)
                                            wObj.put("meanings", w.meanings)
                                            wObj.put("examples", w.examples)
                                            wObj.put("synonyms", w.synonyms)
                                            wObj.put("antonyms", w.antonyms)
                                            wObj.put("wordFamily", w.wordFamily)
                                            wObj.put("level", w.level)
                                            wObj.put("topic", w.topic)
                                            wObj.put("type", w.type)
                                            wObj.put("boxIndex", w.boxIndex)
                                            arr.put(wObj)
                                        }
                                        obj.put("words", arr)
                                        obj.toString()
                                    }
                                    // Save to clipboard
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("7ticks_box_backup", backupJson)
                                    clipboard.setPrimaryClip(clip)
                                    ScaffoldMessenger.showToast(context, "Box backup JSON copied to Clipboard!")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Permanently", color = Color(0xFFFF5252)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    boxRepo.deleteCustomBox(box)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(160.dp))
        }
    }

        // Smart FAB at the bottom-right corner of the Box
        SevenTicksFAB(
            onClick = onNavigateToCreateBox,
            icon = Icons.Default.Add,
            label = "Create Box",
            isExpanded = isFabExpanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 175.dp, end = 24.dp)
        )
    }
}

// Toast Helper Utility
object ScaffoldMessenger {
    fun showToast(context: Context, text: String) {
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG).show()
    }
}

// ====================================================
// 2. CREATE BOX SCREEN
// ====================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CreateEditBoxScreen(
    boxId: Int?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var nameShakeTrigger by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Folder") }
    var selectedColor by remember { mutableStateOf("#00C2FF") }

    val isEditing = boxId != null

    // Load initial values if editing
    LaunchedEffect(boxId) {
        if (boxId != null) {
            val box = boxRepo.getCustomBoxById(boxId)
            if (box != null) {
                name = box.name
                description = box.description
                selectedIcon = box.iconName
                selectedColor = box.colorHex
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = if (isEditing) "Edit Vocab Box" else "Create Custom Box",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Name Input
                SharedTextField(
                    value = name,
                    onValueChange = { 
                        name = it 
                        if (it.trim().isNotEmpty()) {
                            nameError = false
                        }
                    },
                    label = "Box Name",
                    placeholder = "e.g. IELTS Vocabulary",
                    isError = nameError,
                    triggerShake = nameShakeTrigger,
                    onShakeFinished = { nameShakeTrigger = false },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description Input
                SharedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (Optional)",
                    placeholder = "e.g. Challenging C1 academic vocabulary",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon Picker Section
                Text("Select Box Icon", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ICON_LIST.forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x3D00C2FF) else Color(0x06FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF00C2FF) else Color(0x12FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedIcon = iconName
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getBoxIcon(iconName),
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Color Picker Section
                Text("Select Accent Color", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(COLOR_PICKERS) { colorHex ->
                        val isSelected = selectedColor == colorHex
                        val parsedColor = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedColor = colorHex
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                 // Save button
                PremiumGlassButton(
                    text = if (isEditing) "Save Changes" else "Create Box",
                    onClick = {
                        if (name.trim().isNotEmpty()) {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isEditing) {
                                    val original = boxRepo.getCustomBoxById(boxId!!)
                                    if (original != null) {
                                        boxRepo.updateCustomBox(
                                            original.copy(
                                                name = name.trim(),
                                                description = description.trim(),
                                                iconName = selectedIcon,
                                                colorHex = selectedColor,
                                                lastActivityAt = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                } else {
                                    boxRepo.createCustomBox(
                                        name = name.trim(),
                                        description = description.trim(),
                                        iconName = selectedIcon,
                                        colorHex = selectedColor
                                    )
                                }
                                onBack()
                            }
                        } else {
                            nameError = true
                            nameShakeTrigger = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
    }
}

// ====================================================
// 3. BOX DETAIL (WORDS LIST)
// ====================================================
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoxDetailScreen(
    boxId: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onNavigateToAddWord: (Int) -> Unit,
    onNavigateToEditWord: (Int, Int) -> Unit,
    onNavigateToWordDetail: (Int, Int) -> Unit,
    onNavigateToStudy: (Int) -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var boxName by remember { mutableStateOf("Vocab Box") }
    var boxColorHex by remember { mutableStateOf("#00C2FF") }
    var boxIcon by remember { mutableStateOf("Folder") }

    val words by boxRepo.getWordsInCustomBox(boxId).collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    // Load Box Meta details
    LaunchedEffect(boxId) {
        val box = boxRepo.getCustomBoxById(boxId)
        if (box != null) {
            boxName = box.name
            boxColorHex = box.colorHex
            boxIcon = box.iconName
        }
    }

    val boxColor = Color(android.graphics.Color.parseColor(boxColorHex))

    val filteredWords = words.filter {
        it.word.contains(searchQuery, ignoreCase = true) || it.meanings.contains(searchQuery, ignoreCase = true)
    }

    val masteredCount = words.count { it.boxIndex == 7 }
    val progressPercent = if (words.isNotEmpty()) (masteredCount * 100) / words.size else 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navigation Header
            Row(
                modifier = with(sharedTransitionScope) {
                    Modifier
                        .sharedElement(
                            rememberSharedContentState(key = "box_card_${boxId}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .fillMaxWidth()
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boxColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = getBoxIcon(boxIcon), contentDescription = null, tint = boxColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(boxName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${words.size} terms inside", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
            }

            // Stats card showing Leitner distribution
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Leitner Box Progress", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$masteredCount Mastered", color = Color(0xFF00E676), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("•", color = Color.White.copy(alpha = 0.3f))
                            Text("${words.size - masteredCount} Learning", color = Color(0xFF00C2FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { if (words.isNotEmpty()) masteredCount.toFloat() / words.size.toFloat() else 0f },
                            color = boxColor,
                            trackColor = Color(0x11FFFFFF),
                            modifier = Modifier
                                .width(150.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                    Text("$progressPercent%", color = boxColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }

            // Live list Search
            SharedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search Words",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Custom list of words
            if (filteredWords.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    TickyCard(
                        message = "No words inside this box matched your search. Tap '+' to create/auto-fill new terms!",
                        sizeDp = 60,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredWords) { item ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToWordDetail(boxId, item.id)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(item.word, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x1F00C2FF))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(item.type, color = Color(0xFF00C2FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x1FE040FB))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("Leitner ${item.boxIndex}", color = Color(0xFFE040FB), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    val firstMeaning = item.meanings.split("\n").firstOrNull { it.isNotEmpty() } ?: ""
                                    if (firstMeaning.isNotEmpty()) {
                                        Text(firstMeaning, color = Color(0xFFFFD600), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row {
                                    IconButton(onClick = { onNavigateToEditWord(boxId, item.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            boxRepo.removeBoxWordById(item.id)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252).copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (Round Play + Add Word FAB side-by-side)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (words.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                val dueWordsCount = words.count { it.dueDate <= currentTime }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 12.dp, shape = CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (dueWordsCount > 0) {
                                    listOf(Color(0xFFFF5252), Color(0xFFFF1744))
                                } else {
                                    listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
                                }
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToStudy(boxId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Review Box",
                        tint = if (dueWordsCount > 0) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                    if (dueWordsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$dueWordsCount",
                                color = Color(0xFFFF1744),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            SevenTicksFAB(
                onClick = { onNavigateToAddWord(boxId) },
                icon = Icons.Default.Add,
                label = "Add Word",
                isExpanded = true,
                modifier = Modifier
            )
        }
    }
}

// ====================================================
// 4. ADD / EDIT WORD SCREEN WITH AUTO-FILL
// ====================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AddWordScreen(
    boxId: Int,
    boxWordId: Int?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val dictRepo = remember { SevenTicksApplication.instance.dictionaryRepository }
    val searchRepo = remember { SevenTicksApplication.instance.searchRepository }
    
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val isEditing = boxWordId != null

    // Input States
    var searchWordQuery by remember { mutableStateOf("") }
    var liveSearchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var word by remember { mutableStateOf("") }
    var wordError by remember { mutableStateOf(false) }
    var wordShakeTrigger by remember { mutableStateOf(false) }
    var meaning by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var examples by remember { mutableStateOf("") }
    var phoneticsUs by remember { mutableStateOf("") }
    var phoneticsUk by remember { mutableStateOf("") }
    var synonyms by remember { mutableStateOf("") }
    var antonyms by remember { mutableStateOf("") }
    var wordFamily by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("B1") }
    var topic by remember { mutableStateOf("General") }
    var type by remember { mutableStateOf("Noun") }

    // Leitner Box inside Custom Box
    var boxIndex by remember { mutableIntStateOf(1) }

    // Load initial edit values
    LaunchedEffect(boxWordId) {
        if (boxWordId != null) {
            val item = boxRepo.getBoxWordById(boxWordId)
            if (item != null) {
                word = item.word
                meaning = item.meanings
                definition = item.definitions
                examples = item.examples
                phoneticsUs = item.phoneticsUs ?: ""
                phoneticsUk = item.phoneticsUk ?: ""
                synonyms = item.synonyms
                antonyms = item.antonyms
                wordFamily = item.wordFamily
                level = item.level
                topic = item.topic
                type = item.type
                boxIndex = item.boxIndex
            }
        }
    }

    // Live search query for auto-fill typed match
    LaunchedEffect(searchWordQuery) {
        if (searchWordQuery.trim().isEmpty()) {
            liveSearchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            delay(100)
            liveSearchResults = withContext(Dispatchers.IO) {
                searchRepo.search(searchWordQuery.trim(), limit = 8)
            }
            isSearching = false
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = if (isEditing) "Edit Word Details" else "Add New Word",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        // --- Live Search Autofill (only in creation mode) ---
        if (!isEditing) {
            Text("Auto-Fill from Dictionary Source", color = Color(0xFF00FFD2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            SharedTextField(
                value = searchWordQuery,
                onValueChange = { searchWordQuery = it },
                placeholder = "Type to lookup & auto-fill fields...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00C2FF)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (isSearching) {
                LinearProgressIndicator(color = Color(0xFF00C2FF), modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(visible = liveSearchResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Search Matches:", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        liveSearchResults.forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val details = withContext(Dispatchers.IO) {
                                                dictRepo.getWordDetails(result.word)
                                            }
                                            if (details != null) {
                                                word = details.word
                                                phoneticsUs = details.phoneticsUs ?: ""
                                                phoneticsUk = details.phoneticsUk ?: ""
                                                definition = details.definitions.joinToString("\n")
                                                meaning = details.meanings.joinToString("\n")
                                                examples = details.examples.joinToString("\n")
                                                synonyms = details.synonyms.joinToString(",")
                                                antonyms = details.antonyms.joinToString(",")
                                                wordFamily = details.wordFamily.joinToString(",")
                                                level = details.level
                                                topic = details.topic
                                                type = details.type
                                            } else {
                                                // fallback mapping
                                                word = result.word
                                                definition = result.shortMeaning
                                                level = result.level
                                                topic = result.topic
                                                type = result.type
                                            }
                                            searchWordQuery = ""
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(result.word, color = Color(0xFF00FFD2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(result.type, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    Text("•", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                                    Text(result.level, color = Color(0xFFE040FB), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Custom/Auto-filled Fields Section ---
        Text("Word Schema Fields", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Word
                SharedTextField(
                    value = word,
                    onValueChange = { 
                        word = it 
                        if (it.trim().isNotEmpty()) {
                            wordError = false
                        }
                    },
                    label = "Word",
                    isError = wordError,
                    triggerShake = wordShakeTrigger,
                    onShakeFinished = { wordShakeTrigger = false },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Meanings / Translation
                SharedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = "Meanings / Translations (one per line)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Definition
                SharedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = "Definitions (one per line)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Examples
                SharedTextField(
                    value = examples,
                    onValueChange = { examples = it },
                    label = "Examples (one per line)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Phonetics
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SharedTextField(
                        value = phoneticsUs,
                        onValueChange = { phoneticsUs = it },
                        label = "Phonetics US",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SharedTextField(
                        value = phoneticsUk,
                        onValueChange = { phoneticsUk = it },
                        label = "Phonetics UK",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Synonyms & Antonyms
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SharedTextField(
                        value = synonyms,
                        onValueChange = { synonyms = it },
                        label = "Synonyms (comma separated)",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SharedTextField(
                        value = antonyms,
                        onValueChange = { antonyms = it },
                        label = "Antonyms (comma separated)",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Word Family
                SharedTextField(
                    value = wordFamily,
                    onValueChange = { wordFamily = it },
                    label = "Word Family (comma separated)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Level, Topic, Type metadata
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SharedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = "Level",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SharedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = "Type",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SharedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = "Category",
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // SAVE WORD Action
                PremiumGlassButton(
                    text = if (isEditing) "Save Word Changes" else "Add Word to Box",
                    onClick = {
                        if (word.trim().isNotEmpty()) {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isEditing) {
                                    val item = boxRepo.getBoxWordById(boxWordId!!)
                                    if (item != null) {
                                        boxRepo.updateBoxWord(
                                            item.copy(
                                                word = word.trim(),
                                                meanings = meaning.trim(),
                                                definitions = definition.trim(),
                                                examples = examples.trim(),
                                                phoneticsUs = phoneticsUs.trim().ifEmpty { null },
                                                phoneticsUk = phoneticsUk.trim().ifEmpty { null },
                                                synonyms = synonyms.trim(),
                                                antonyms = antonyms.trim(),
                                                wordFamily = wordFamily.trim(),
                                                level = level.trim(),
                                                topic = topic.trim(),
                                                type = type.trim()
                                            )
                                        )
                                    }
                                } else {
                                    boxRepo.addWordToCustomBox(
                                        BoxWordEntity(
                                            boxId = boxId,
                                            wordId = 0,
                                            word = word.trim(),
                                            meanings = meaning.trim(),
                                            definitions = definition.trim(),
                                            examples = examples.trim(),
                                            phoneticsUs = phoneticsUs.trim().ifEmpty { null },
                                            phoneticsUk = phoneticsUk.trim().ifEmpty { null },
                                            synonyms = synonyms.trim(),
                                            antonyms = antonyms.trim(),
                                            wordFamily = wordFamily.trim(),
                                            level = level.trim(),
                                            topic = topic.trim(),
                                            type = type.trim(),
                                            boxIndex = 1
                                        )
                                    )
                                }
                                onBack()
                            }
                        } else {
                            wordError = true
                            wordShakeTrigger = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ====================================================
// 5. WORD DETAILS SCREEN (DEDICATED VISUAL PAGE)
// ====================================================
@Composable
fun BoxWordDetailScreen(
    boxId: Int,
    boxWordId: Int,
    onBack: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var item by remember { mutableStateOf<BoxWordEntity?>(null) }

    // Load custom box word
    LaunchedEffect(boxWordId) {
        item = boxRepo.getBoxWordById(boxWordId)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Word Portfolio",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        if (item == null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00C2FF))
            }
        } else {
            val word = item!!

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Title block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(word.word, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(word.type, color = Color(0xFF00C2FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("•", color = Color.White.copy(alpha = 0.3f))
                                Text(word.level, color = Color(0xFFE040FB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("•", color = Color.White.copy(alpha = 0.3f))
                                Text("Box ${word.boxIndex}", color = Color(0xFFFFD600), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Star badge icon (quick info)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1F00FFD2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF00FFD2), modifier = Modifier.size(18.dp))
                        }
                    }

                    // Phonetics Us / Uk
                    if (word.phoneticsUs != null || word.phoneticsUk != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            word.phoneticsUs?.let { us ->
                                Text("US: $us", color = Color(0xFF00FFD2), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                            word.phoneticsUk?.let { uk ->
                                Text("UK: $uk", color = Color(0xFFFFD600), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0x1FFFFFFF))

                    // Persian translations / meanings
                    if (word.meanings.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Translations / Persian meanings", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.meanings.split("\n").filter { it.isNotEmpty() }.forEach { m ->
                                Text(m, color = Color(0xFFFFD600), fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                    }

                    // English definitions
                    if (word.definitions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Definitions", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.definitions.split("\n").filter { it.isNotEmpty() }.forEach { def ->
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("•", color = Color(0xFF00C2FF), fontWeight = FontWeight.Bold)
                                    Text(def, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 19.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                    }

                    // Sentence Examples
                    if (word.examples.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Sentence Examples", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.examples.split("\n").filter { it.isNotEmpty() }.forEach { ex ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x0AFFFFFF))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "\"$ex\"",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                    }

                    // Synonyms / Antonyms
                    if (word.synonyms.isNotEmpty() || word.antonyms.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (word.synonyms.isNotEmpty()) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Synonyms", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(word.synonyms, color = Color(0xFF00E676), fontSize = 13.sp)
                                }
                            }
                            if (word.antonyms.isNotEmpty()) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Antonyms", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(word.antonyms, color = Color(0xFFFF7043), fontSize = 13.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                    }

                    // Word Family
                    if (word.wordFamily.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Word Family", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(word.wordFamily, color = Color(0xFFE040FB), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================
// 6. BOX LEARNING / REVIEW SESSIONS
// ====================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxStudyScreen(
    boxId: Int,
    onBack: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val repo = remember { SevenTicksApplication.instance.userRepository }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val userProgress by repo.userProgress.collectAsState(initial = null)

    var cardsToReview by remember { mutableStateOf<List<BoxWordEntity>>(emptyList()) }
    var allCardsInBox by remember { mutableStateOf<List<BoxWordEntity>>(emptyList()) }
    var initialReviewLogs by remember { mutableStateOf<List<ReviewHistoryEntity>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(true) }
    var boxName by remember { mutableStateOf("Vocab Box") }

    // Session status trackers
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var sessionFinished by remember { mutableStateOf(false) }
    var showLeveledUpDialog by remember { mutableStateOf(false) }

    // Setup the unified session queue engine dynamically once cards load
    val engine = remember(cardsToReview, initialReviewLogs) {
        if (cardsToReview.isEmpty()) null else {
            val items = cardsToReview.map { word ->
                val wordReviewHistory = initialReviewLogs.filter { it.wordId == word.wordId }.sortedBy { it.timestamp }
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
            val queueManager = SessionQueueManager(items)
            StudySessionEngine(
                queueManager = queueManager,
                scope = coroutineScope,
                initialStreak = 0,
                onCorrectHook = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onWrongHook = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onSessionFinished = { sessionFinished = true }
            )
        }
    }

    // Connect queue flows to state
    val activeIndex by engine?.queueManager?.currentIndex?.collectAsState() ?: remember { mutableStateOf(0) }
    val currentItem by engine?.queueManager?.currentItem?.collectAsState() ?: remember { mutableStateOf(null) }

    val activeItem = currentItem ?: if (cardsToReview.isNotEmpty()) {
        val word = cardsToReview.first()
        val wordReviewHistory = initialReviewLogs.filter { it.wordId == word.wordId }.sortedBy { it.timestamp }
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

    val currentWord = activeItem?.payload as? BoxWordEntity ?: if (cardsToReview.isNotEmpty()) {
        cardsToReview.first()
    } else {
        BoxWordEntity(boxId = 0, wordId = 0, word = "")
    }

    // Reload list on start
    LaunchedEffect(boxId) {
        isLoading = true
        val box = boxRepo.getCustomBoxById(boxId)
        if (box != null) boxName = box.name

        initialReviewLogs = repo.getReviewHistoryOnce()

        val list = boxRepo.getWordsInCustomBoxOnce(boxId)
        allCardsInBox = list

        cardsToReview = list
        currentIndex = 0
        isFlipped = false
        sessionFinished = false
        isLoading = false
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
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f + bgShift * 3f, 1500f - bgShift * 2f)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = boxName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00C2FF))
                }
            } else if (sessionFinished || cardsToReview.isEmpty()) {
                // FINISHED / EMPTY STATE
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TickyCard(
                        message = "Superb job! You finished reviewing words inside $boxName! Your memory is now highly calibrated!",
                        sizeDp = 80,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumGlassButton(
                        text = "Return to Box",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // 1. Session Header Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level ${userProgress?.level ?: 1} | +${engine?.xpEarned ?: 0} XP",
                            color = Color(0xFFFFD600),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        val total = cardsToReview.size
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
                            val total = cardsToReview.size
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // State Breakdown Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val newCount = cardsToReview.count { it.state == 0 }
                        val learnCount = cardsToReview.count { it.state == 1 }
                        val relearnCount = cardsToReview.count { it.state == 3 }
                        val dueCount = cardsToReview.count { it.state == 2 }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "New: $newCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFEA00)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Learn: $learnCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFF9100)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Relearn: $relearnCount", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E676)))
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
                                (item.payload as? BoxWordEntity)?.word ?: ""
                            },
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                // 2. High-fidelity Unified Spaced Repetition Flashcard
                val boxCircleStates = remember(activeItem, initialReviewLogs, engine?.temporaryOverlayIndex, engine?.temporaryOverlayRating) {
                    val activeWord = activeItem?.payload as? BoxWordEntity
                    val baseCircles = if (activeWord != null) {
                        val wordReviewHistory = initialReviewLogs.filter { it.wordId == activeWord.wordId }.sortedBy { it.timestamp }
                        List(7) { i ->
                            if (i < wordReviewHistory.size) {
                                when (wordReviewHistory[i].rating) {
                                    4 -> "Green"
                                    3 -> "Blue"
                                    2 -> "Yellow"
                                    else -> "Red"
                                }
                            } else "Gray"
                        }
                    } else List(7) { "Gray" }

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

                fun handleBoxRating(rating: ReviewRatingModel) {
                    val activeEngine = engine ?: return
                    val item = activeItem ?: return
                    val word = item.payload as? BoxWordEntity ?: return
                    val currentBoxIdx = word.boxIndex
                    val nextBoxIdx = when (rating) {
                        ReviewRatingModel.AGAIN -> 1
                        ReviewRatingModel.HARD -> (currentBoxIdx - 1).coerceAtLeast(1)
                        ReviewRatingModel.GOOD -> (currentBoxIdx + 1).coerceAtMost(7)
                        ReviewRatingModel.EASY -> (currentBoxIdx + 2).coerceAtMost(7)
                    }
                    val promotedToBox7 = currentBoxIdx < 7 && nextBoxIdx == 7

                    activeEngine.submitRating(
                        rating = rating,
                        currentCircleIndex = (word.boxIndex - 1).coerceIn(0, 6),
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
                                    cardId = word.id,
                                    isBoxWord = true,
                                    rating = rating
                                )
                                if (leveledUp) {
                                    showLeveledUpDialog = true
                                    activeEngine.triggerEvent(com.example.core.learning.CompanionEvent.LevelUp)
                                }
                            }
                        }
                    )
                }

                val wordDetails = remember(currentWord) {
                    currentWord.toWordDetails()
                }

                Spacer(modifier = Modifier.height(12.dp))

                com.example.core.ui.components.flashcard.FlashcardScreen(
                    wordDetails = wordDetails,
                    isFlipped = isFlipped,
                    onFlip = { isFlipped = !isFlipped },
                    onRatingClick = { handleBoxRating(it) },
                    circleStates = boxCircleStates,
                    againSubtext = "",
                    hardSubtext = "",
                    goodSubtext = "",
                    easySubtext = "",
                    tikiMessage = engine?.tikiReactionMessage ?: "Keep going! You're doing amazing!",
                    tikiState = engine?.tikiState ?: "st-happy",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
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
}

// ====================================================
// 7. IMPORT BACKUP SCREEN
// ====================================================
@Composable
fun ImportBackupScreen(
    onBack: () -> Unit
) {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var jsonText by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Import Box JSON",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Paste the copied vocabulary box backup JSON text here. We will recreate the box and restore all elements.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    placeholder = { Text("Paste JSON here...", color = Color.White.copy(alpha = 0.4f)) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0x0AFFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                if (isImporting) {
                    CircularProgressIndicator(color = Color(0xFF00C2FF), modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    PremiumGlassButton(
                        text = "Parse & Restore Box",
                        onClick = {
                            if (jsonText.trim().isNotEmpty()) {
                                coroutineScope.launch {
                                    isImporting = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val success = withContext(Dispatchers.IO) {
                                        try {
                                            val obj = JSONObject(jsonText.trim())
                                            val name = obj.getString("name")
                                            val desc = obj.optString("description", "")
                                            val iconName = obj.optString("iconName", "Folder")
                                            val colorHex = obj.optString("colorHex", "#00C2FF")

                                            // Recreate the custom box
                                            val newBoxId = boxRepo.createCustomBox(name, desc, iconName, colorHex)
                                            
                                            // Re-insert words
                                            val wordsArr = obj.getJSONArray("words")
                                            for (i in 0 until wordsArr.length()) {
                                                val wObj = wordsArr.getJSONObject(i)
                                                boxRepo.addWordToCustomBox(
                                                    BoxWordEntity(
                                                        boxId = newBoxId.toInt(),
                                                        wordId = 0,
                                                        word = wObj.getString("word"),
                                                        phoneticsUs = wObj.optString("phoneticsUs", "").ifEmpty { null },
                                                        phoneticsUk = wObj.optString("phoneticsUk", "").ifEmpty { null },
                                                        definitions = wObj.optString("definitions", ""),
                                                        meanings = wObj.optString("meanings", ""),
                                                        examples = wObj.optString("examples", ""),
                                                        synonyms = wObj.optString("synonyms", ""),
                                                        antonyms = wObj.optString("antonyms", ""),
                                                        wordFamily = wObj.optString("wordFamily", ""),
                                                        level = wObj.optString("level", "B1"),
                                                        topic = wObj.optString("topic", "General"),
                                                        type = wObj.optString("type", "Noun"),
                                                        boxIndex = wObj.optInt("boxIndex", 1)
                                                    )
                                                )
                                            }
                                            true
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            false
                                        }
                                    }
                                    isImporting = false
                                    if (success) {
                                        ScaffoldMessenger.showToast(context, "Box backup imported successfully!")
                                        onBack()
                                    } else {
                                        ScaffoldMessenger.showToast(context, "Failed to parse JSON. Please verify it is a valid box export string!")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
