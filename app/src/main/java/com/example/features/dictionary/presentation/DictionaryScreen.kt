package com.example.features.dictionary.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.example.ui.theme.isDark
import com.example.core.database.BoxWordEntity
import com.example.core.database.CustomBoxEntity
import com.example.core.database.FavoriteWordEntity
import com.example.core.database.RecentSearchEntity
import com.example.core.database.SearchResult
import com.example.core.database.WordDetails
import com.example.core.ui.components.SharedTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen() {
    val boxRepo = remember { SevenTicksApplication.instance.boxRepository }
    val dictRepo = remember { SevenTicksApplication.instance.dictionaryRepository }
    val searchRepo = remember { SevenTicksApplication.instance.searchRepository }
    
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var query by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Database state flows
    val recentSearches by boxRepo.getRecentSearches(10).collectAsState(initial = emptyList())
    val favoriteWords by boxRepo.getFavoriteWords().collectAsState(initial = emptyList())
    val customBoxes by boxRepo.getCustomBoxes().collectAsState(initial = emptyList())

    // Active search results
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Word Details Modal State
    var selectedWord by remember { mutableStateOf<WordDetails?>(null) }
    var showDetailsModal by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showDetailsModal) {
        showDetailsModal = false
    }
    var isWordFavorite by remember { mutableStateOf(false) }

    // Dropdown for adding to a Custom Box
    var showAddToBoxDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.features.tiki.api.TikiController.getInstance().triggerPipeline(
            contextEvent = com.example.features.tiki.context.ContextEvent.Custom("Idle"),
            behaviorEvent = com.example.features.tiki.behavior.BehaviorEvent.DictionaryOpened
        )
    }

    // Live search prefix trigger while typing
    LaunchedEffect(query) {
        if (query.trim().isEmpty()) {
            searchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            delay(150) // Debounce searching
            val results = withContext(Dispatchers.IO) {
                searchRepo.search(query.trim(), limit = 60)
            }
            searchResults = results
            isSearching = false
            com.example.features.tiki.api.TikiController.getInstance().triggerPipeline(
                contextEvent = com.example.features.tiki.context.ContextEvent.Custom("Idle"),
                behaviorEvent = com.example.features.tiki.behavior.BehaviorEvent.WordSearched(query.trim(), results.isNotEmpty())
            )
        }
    }

    val isDark = MaterialTheme.colorScheme.isDark
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)

    // Main scroll container (FIXED Header)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x1F00FFD2) else Color(0x1F0284C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF00FFD2) else Color(0xFF0284C7),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "Lexicon Explorer",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Search thousands of curated academic terms",
                    color = subtextColor,
                    fontSize = 11.sp
                )
            }
        }

        // Search Text Field Input
        SharedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search Words",
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isDark) Color(0xFF00C2FF) else Color(0xFF0284C7)) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { 
                        query = "" 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textColor)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Conditional display based on query state
            if (query.trim().isEmpty()) {
            // --- RECENT SEARCHES PANEL ---
            if (recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, tint = subtextColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Recent Searches",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Clear All",
                        color = if (isDark) Color(0xFF00C2FF) else Color(0xFF0284C7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                boxRepo.clearRecentSearches()
                            }
                        }
                    )
                }

                // Recent searches tags
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentSearches.forEach { search ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x11FFFFFF) else Color(0x0C0F172A))
                                .border(1.dp, if (isDark) Color(0x0EFFFFFF) else Color(0x1A0F172A), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    query = search.query
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = search.query,
                                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B),
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF64748B),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable {
                                            coroutineScope.launch {
                                                boxRepo.deleteRecentSearch(search.query)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // --- FAVORITES PANEL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(18.dp))
                Text(
                    text = "My Starred Words",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (favoriteWords.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No Starred Words Yet",
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Any dictionary word you star will appear here for fast, offline access.",
                            color = subtextColor,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                favoriteWords.forEach { favorite ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val details = withContext(Dispatchers.IO) {
                                    dictRepo.getWordDetails(favorite.word)
                                }
                                if (details != null) {
                                    selectedWord = details
                                    isWordFavorite = true
                                    showDetailsModal = true
                                    boxRepo.addRecentSearch(favorite.word)
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(16.dp))
                                Text(
                                    text = favorite.word,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    boxRepo.removeFavoriteWord(favorite.word)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Unstar", tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Tiki Mascot Placement
            TickyCard(
                message = "Explore word structures, synonyms, and levels! Star terms to find them quickly.",
                sizeDp = 50,
                modifier = Modifier.fillMaxWidth()
            )

        } else {
            // --- SEARCH RESULTS PANEL ---
            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = if (isDark) Color(0xFF00C2FF) else Color(0xFF0284C7))
                }
            } else if (searchResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TickyCard(
                        message = "I couldn't find \"$query\" in my database. Let's try typing another term or check the spelling!",
                        sizeDp = 80,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matching Vocabulary (${searchResults.size})",
                        color = subtextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                searchResults.forEach { result ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val details = withContext(Dispatchers.IO) {
                                    dictRepo.getWordDetails(result.word)
                                }
                                val isFav = boxRepo.isFavoriteWord(result.word)
                                selectedWord = details ?: WordDetails(
                                    word = result.word,
                                    level = result.level,
                                    phonetics = "",
                                    definitions = if (result.shortMeaning.isNotEmpty()) listOf(result.shortMeaning) else emptyList(),
                                    translations = emptyList(),
                                    examples = emptyList(),
                                    exampleTranslations = emptyList(),
                                    synonyms = emptyList(),
                                    antonyms = emptyList(),
                                    wordFamily = emptyList(),
                                    collocations = emptyList(),
                                    phrases = emptyList(),
                                    notes = emptyList(),
                                    types = listOf(result.type),
                                    topics = listOf(result.topic)
                                )
                                isWordFavorite = isFav
                                showDetailsModal = true
                                boxRepo.addRecentSearch(result.word)
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.word,
                                    color = if (isDark) Color(0xFF00FFD2) else Color(0xFF0F766E),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDark) Color(0x229D00FF) else Color(0x1F9D00FF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = result.level,
                                            color = if (isDark) Color(0xFFE040FB) else Color(0xFF7C3AED),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDark) Color(0x1F00C2FF) else Color(0x1F0284C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = result.type,
                                            color = if (isDark) Color(0xFF00C2FF) else Color(0xFF0369A1),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (result.shortMeaning.isNotEmpty()) {
                                Text(
                                    text = result.shortMeaning,
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF1E293B),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                            if (result.topic.isNotEmpty() && result.topic != "General") {
                                Text(
                                    text = "Category: ${result.topic}",
                                    color = subtextColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}

    // --- WORD DETAILS PREMIUM DIALOG SHEET ---
    if (showDetailsModal && selectedWord != null) {
        val word = selectedWord!!
        val isDark = MaterialTheme.colorScheme.isDark
        val dialogBg = if (isDark) Color(0xFF0C0D21) else Color.White
        val dialogBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F0F172A)
        val dialogTextColor = if (isDark) Color.White else Color(0xFF0F172A)
        val dialogSubtleTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)
        val dialogStrongTextColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B)
        val dialogMoreSubtleTextColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF475569)
        val dialogFaintTextColor = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF334155)
        val dividerColor = if (isDark) Color(0x1FFFFFFF) else Color(0x1F0F172A)
        val exampleBg = if (isDark) Color(0x0EFFFFFF) else Color(0x0C0F172A)
        val dropdownBg = if (isDark) Color(0xFF0F1026) else Color.White
        val dropdownBorder = if (isDark) Color(0x22FFFFFF) else Color(0x1A0F172A)
        
        AlertDialog(
            onDismissRequest = { showDetailsModal = false },
            containerColor = dialogBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, dialogBorder, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp)),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = word.word,
                            color = dialogTextColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = word.type,
                                color = Color(0xFF00C2FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = dialogSubtleTextColor,
                                fontSize = 12.sp
                            )
                            Text(
                                text = word.level,
                                color = Color(0xFFE040FB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                boxRepo.toggleFavoriteWord(word.word)
                                isWordFavorite = !isWordFavorite
                                if (isWordFavorite) {
                                    com.example.features.tiki.api.TikiController.getInstance().triggerPipeline(
                                        contextEvent = com.example.features.tiki.context.ContextEvent.Custom("Idle"),
                                        behaviorEvent = com.example.features.tiki.behavior.BehaviorEvent.WordStarred(word.word)
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isWordFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isWordFavorite) Color(0xFFFF4081) else dialogMoreSubtleTextColor
                            )
                        }
                        IconButton(onClick = { showDetailsModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = dialogMoreSubtleTextColor)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Phonetics
                    if (word.phoneticsUs != null || word.phoneticsUk != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            word.phoneticsUs?.let { us ->
                                Text(
                                    text = "US: $us",
                                    color = Color(0xFF00FFD2),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            word.phoneticsUk?.let { uk ->
                                Text(
                                    text = "UK: $uk",
                                    color = Color(0xFFFFD600),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor)

                    // Persian / Meanings
                    if (word.meanings.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Translations & Meanings", color = dialogSubtleTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.meanings.forEach { m ->
                                Text(
                                    text = m,
                                    color = Color(0xFFFFD600),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        HorizontalDivider(color = dividerColor)
                    }

                    // Definitions
                    if (word.definitions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Definitions", color = dialogSubtleTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.definitions.forEach { d ->
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "•", color = Color(0xFF00C2FF), fontWeight = FontWeight.Bold)
                                    Text(text = d, color = dialogStrongTextColor, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = dividerColor)
                    }

                    // Examples
                    if (word.examples.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Sentence Examples", color = dialogSubtleTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            word.examples.forEach { ex ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(exampleBg)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "\"$ex\"",
                                        color = dialogFaintTextColor,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = dividerColor)
                    }

                    // Synonyms & Antonyms
                    if (word.synonyms.isNotEmpty() || word.antonyms.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (word.synonyms.isNotEmpty()) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "Synonyms", color = dialogSubtleTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = word.synonyms.joinToString(", "),
                                        color = Color(0xFF00E676),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (word.antonyms.isNotEmpty()) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "Antonyms", color = dialogSubtleTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = word.antonyms.joinToString(", "),
                                        color = Color(0xFFFF7043),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = dividerColor)
                    }

                    // Word Family
                    if (word.wordFamily.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Word Family", color = dialogSubtleTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = word.wordFamily.joinToString(" ➔ "),
                                color = Color(0xFFE040FB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- DIRECT ACTION: ADD TO CUSTOM BOX ---
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PremiumGlassButton(
                            text = "Add to Vocabulary Box",
                            onClick = { showAddToBoxDropdown = true },
                            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showAddToBoxDropdown,
                            onDismissRequest = { showAddToBoxDropdown = false },
                            modifier = Modifier
                                .background(dropdownBg)
                                .border(1.dp, dropdownBorder, RoundedCornerShape(8.dp))
                        ) {
                            if (customBoxes.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No boxes created yet", color = dialogSubtleTextColor) },
                                    onClick = { showAddToBoxDropdown = false }
                                )
                            } else {
                                customBoxes.forEach { box ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(android.graphics.Color.parseColor(box.colorHex)), modifier = Modifier.size(16.dp))
                                                Text(box.name, color = dialogTextColor)
                                            }
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Convert lists to strings
                                                val defStr = word.definitions.joinToString("\n")
                                                val meanStr = word.meanings.joinToString("\n")
                                                val exStr = word.examples.joinToString("\n")
                                                val synStr = word.synonyms.joinToString(",")
                                                val antStr = word.antonyms.joinToString(",")
                                                val famStr = word.wordFamily.joinToString(",")

                                                boxRepo.addWordToCustomBox(
                                                    BoxWordEntity(
                                                        boxId = box.id,
                                                        wordId = 0,
                                                        word = word.word,
                                                        phoneticsUs = word.phoneticsUs,
                                                        phoneticsUk = word.phoneticsUk,
                                                        definitions = defStr,
                                                        meanings = meanStr,
                                                        examples = exStr,
                                                        synonyms = synStr,
                                                        antonyms = antStr,
                                                        wordFamily = famStr,
                                                        level = word.level,
                                                        topic = word.topic,
                                                        type = word.type,
                                                        boxIndex = 1
                                                    )
                                                )
                                                showAddToBoxDropdown = false
                                                showDetailsModal = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
