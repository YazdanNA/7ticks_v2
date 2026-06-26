package com.example.core.ui.components.flashcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.WordDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMoreDetailsDialog(
    wordDetails: WordDetails,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0C0D21),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wordDetails.word,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wordDetails.type,
                            color = Color(0xFF00C2FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = wordDetails.level,
                            color = Color(0xFFE040FB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
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
                if (wordDetails.phonetics.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Pronunciation: ${wordDetails.phonetics}",
                            color = Color(0xFF00FFD2),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                HorizontalDivider(color = Color(0x1FFFFFFF))

                // Persian / Meanings
                if (wordDetails.translations.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Translations & Meanings",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        wordDetails.translations.forEach { m ->
                            Text(
                                text = m,
                                color = Color(0xFFFFD600),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0x1FFFFFFF))
                }

                // Definitions
                if (wordDetails.definitions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Definitions",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        wordDetails.definitions.forEach { d ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "•", color = Color(0xFF00C2FF), fontWeight = FontWeight.Bold)
                                Text(
                                    text = d,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0x1FFFFFFF))
                }

                // Examples
                if (wordDetails.examples.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Sentence Examples",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        wordDetails.examples.forEach { ex ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x0EFFFFFF))
                                    .padding(8.dp)
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

                // Synonyms & Antonyms
                if (wordDetails.synonyms.isNotEmpty() || wordDetails.antonyms.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (wordDetails.synonyms.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Synonyms",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = wordDetails.synonyms.joinToString(", "),
                                    color = Color(0xFF00E676),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (wordDetails.antonyms.isNotEmpty()) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Antonyms",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = wordDetails.antonyms.joinToString(", "),
                                    color = Color(0xFFFF7043),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0x1FFFFFFF))
                }

                // Word Family
                if (wordDetails.wordFamily.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Word Family",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = wordDetails.wordFamily.joinToString(" ➔ "),
                            color = Color(0xFFE040FB),
                            fontSize = 12.sp
                        )
                    }
                    HorizontalDivider(color = Color(0x1FFFFFFF))
                }

                // Collocations & Phrases & Notes
                if (wordDetails.collocations.isNotEmpty() || wordDetails.phrases.isNotEmpty() || wordDetails.notes.isNotEmpty()) {
                    if (wordDetails.collocations.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Collocations", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = wordDetails.collocations.joinToString(", "), color = Color(0xFF00FFD2), fontSize = 12.sp)
                        }
                    }
                    if (wordDetails.phrases.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Phrases", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = wordDetails.phrases.joinToString(", "), color = Color(0xFFFFD600), fontSize = 12.sp)
                        }
                    }
                    if (wordDetails.notes.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Notes", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = wordDetails.notes.joinToString("\n"), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
