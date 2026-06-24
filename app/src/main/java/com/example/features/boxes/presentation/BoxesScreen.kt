package com.example.features.boxes.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.components.TikiPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxesScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newBoxName by remember { mutableStateOf("") }
    
    // Dynamic mock list
    val defaultBoxes = remember {
        mutableStateListOf(
            BoxItem("English Basics", 442, 1200, 37, Color(0xFF00C2FF)),
            BoxItem("IELTS Vocabulary", 902, 1500, 61, Color(0xFF9D00FF)),
            BoxItem("Phrasal Verbs", 320, 1300, 24, Color(0xFFFFD600)),
            BoxItem("Daily Words", 128, 150, 85, Color(0xFF00E676))
        )
    }

    val filteredBoxes = defaultBoxes.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search your boxes...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x0AFFFFFF),
                unfocusedContainerColor = Color(0x0AFFFFFF),
                focusedIndicatorColor = Color(0xFF00C2FF),
                unfocusedIndicatorColor = Color(0x22FFFFFF),
                cursorColor = Color(0xFF00C2FF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x0AFFFFFF))
        )

        // 2. Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Vocab Boxes",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            PremiumGlassButton(
                text = "Create Box",
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                modifier = Modifier.wrapContentSize()
            )
        }

        // 3. Main Boxes List or Empty state
        if (filteredBoxes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Vocabulary Boxes Found",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create a custom box or adjust your search to begin storing items.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            filteredBoxes.forEach { box ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* View box details placeholder */ }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(box.accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = box.accentColor
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
                                    Text(
                                        text = "${box.activeWords} / ${box.totalWords} words mastered",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = "${box.percentage}%",
                                color = box.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        // Progression indicator
                        LinearProgressIndicator(
                            progress = { box.percentage.toFloat() / 100f },
                            color = box.accentColor,
                            trackColor = Color(0x1AFFFFFF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // 4. Tiki Helper mascot spot
        Spacer(modifier = Modifier.height(16.dp))
        TikiPlaceholder(
            message = "You can group and organize cards by categories! This helps your memory associations.",
            sizeDp = 60,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Interactive Create Box Dialog Skeleton
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF0F1026),
            title = {
                Text(
                    text = "Create New Box",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Design a customized vocabulary set. Enter a name to establish its folder inside your spaced repetition storage.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = newBoxName,
                        onValueChange = { newBoxName = it },
                        placeholder = { Text("e.g. Phrasal Verbs", color = Color.White.copy(alpha = 0.4f)) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x0AFFFFFF),
                            unfocusedContainerColor = Color(0x0AFFFFFF)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBoxName.isNotBlank()) {
                            defaultBoxes.add(
                                BoxItem(
                                    name = newBoxName,
                                    activeWords = 0,
                                    totalWords = 50,
                                    percentage = 0,
                                    accentColor = Color(0xFF00FFD2)
                                )
                            )
                            newBoxName = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Create Box", color = Color(0xFF00FFD2), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

data class BoxItem(
    val name: String,
    val activeWords: Int,
    val totalWords: Int,
    val percentage: Int,
    val accentColor: Color
)
