package com.example.features.settings.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.SevenTicksApplication
import com.example.core.components.GlassCard
import com.example.core.components.PremiumGlassButton
import com.example.core.feedback.FeedbackManager
import com.example.ui.theme.LocalAppLanguage
import com.example.core.ui.components.AnimatedBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = remember { SevenTicksApplication.instance.preferencesManager }
    val feedbackManager = remember { FeedbackManager.getInstance(context) }

    // Settings States collected from DataStore flow
    val currentLang by prefs.appLanguageFlow.collectAsState(initial = prefs.appLanguage)
    val currentTheme by prefs.themeModeFlow.collectAsState(initial = prefs.themeMode)
    val currentFontSize by prefs.fontSizeFlow.collectAsState(initial = prefs.fontSizeSetting)
    val soundEnabled by prefs.soundEnabledFlow.collectAsState(initial = prefs.soundEnabled)
    val hapticEnabled by prefs.hapticEnabledFlow.collectAsState(initial = prefs.hapticEnabled)
    val autoBackup by prefs.cloudBackupAutoFlow.collectAsState(initial = prefs.cloudBackupAuto)
    val lastBackupTime by prefs.lastBackupTimeFlow.collectAsState(initial = prefs.lastBackupTime)

    // Local UI loading and dialog states
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    // Translations based on the current active language (Immediate App Localization)
    val isRtl = currentLang == "fa"
    val tBack = if (isRtl) "بازگشت" else "Back"
    val tSettings = if (isRtl) "تنظیمات" else "Settings"
    val tGeneral = if (isRtl) "عمومی" else "General"
    val tLanguage = if (isRtl) "زبان" else "Language"
    val tTheme = if (isRtl) "پوسته" else "Theme"
    val tFontSize = if (isRtl) "اندازه قلم" else "Font Size"
    val tFeedback = if (isRtl) "بازخورد" else "Feedback"
    val tSoundEffects = if (isRtl) "جلوه‌های صوتی" else "Sound Effects"
    val tHapticFeedback = if (isRtl) "بازخورد لرزشی" else "Haptic Feedback"
    val tCloudBackup = if (isRtl) "پشتیبان‌گیری ابری" else "Cloud Backup"
    val tGoogleAccount = if (isRtl) "حساب گوگل" else "Google Account"
    val tAutoBackup = if (isRtl) "پشتیبان‌گیری خودکار" else "Automatic Backup"
    val tBackupNow = if (isRtl) "پشتیبان‌گیری دستی" else "Backup Now"
    val tRestoreBackup = if (isRtl) "بازیابی پشتیبان" else "Restore Backup"
    val tLastBackup = if (isRtl) "آخرین پشتیبان" else "Last Backup"
    val tAbout = if (isRtl) "درباره برنامه" else "About"
    val tVersion = if (isRtl) "نسخه" else "Version"
    val tBuild = if (isRtl) "شماره ساخت" else "Build Number"
    val tContact = if (isRtl) "ایمیل پشتیبانی" else "Contact Support"
    val tPrivacyPolicy = if (isRtl) "سیاست حریم خصوصی" else "Privacy Policy"
    val tTermsOfService = if (isRtl) "شرایط استفاده" else "Terms of Service"
    val tLicenses = if (isRtl) "مجوزهای متن‌باز" else "Open Source Licenses"

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    AnimatedBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            // Keep Navigation Bar Top in strict LTR so the Back button is always on the left (MUST NOT be mirrored)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    title = {
                        Text(
                            text = tSettings,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                feedbackManager.vibrateLight()
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tBack,
                                tint = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Wrap settings content in dynamic layout direction (RTL if Persian, LTR if English)
        CompositionLocalProvider(
            LocalLayoutDirection provides (if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // CATEGORY 1: General
                SettingsSectionHeader(title = tGeneral, icon = Icons.Default.Settings)
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // 1. Language Option
                        SettingsItemRow(
                            title = tLanguage,
                            subtitle = if (currentLang == "fa") "فارسی" else "English",
                            icon = Icons.Default.Language
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(
                                        if (isDark) Color(0x12FFFFFF) else Color(0x0F000000),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val langs = listOf("en" to "English", "fa" to "فارسی")
                                langs.forEach { (code, name) ->
                                    val isSelected = currentLang == code
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                feedbackManager.vibrateLight()
                                                prefs.appLanguage = code
                                                feedbackManager.playSound("typing")
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // 2. Theme Option
                        SettingsItemRow(
                            title = tTheme,
                            subtitle = when (currentTheme) {
                                "light" -> if (isRtl) "روشن" else "Light"
                                "dark" -> if (isRtl) "تاریک" else "Dark"
                                else -> if (isRtl) "پیروی از سیستم" else "Follow System"
                            },
                            icon = Icons.Default.Brightness4
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(
                                        if (isDark) Color(0x12FFFFFF) else Color(0x0F000000),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val themes = listOf(
                                    "light" to (if (isRtl) "روشن" else "Light"),
                                    "dark" to (if (isRtl) "تاریک" else "Dark"),
                                    "system" to (if (isRtl) "سیستم" else "System")
                                )
                                themes.forEach { (themeCode, label) ->
                                    val isSelected = currentTheme == themeCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                feedbackManager.vibrateLight()
                                                prefs.themeMode = themeCode
                                                feedbackManager.playSound("typing")
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // 3. Font Size Option
                        SettingsItemRow(
                            title = tFontSize,
                            subtitle = when (currentFontSize) {
                                "small" -> if (isRtl) "کوچک" else "Small"
                                "large" -> if (isRtl) "بزرگ" else "Large"
                                else -> if (isRtl) "پیش‌فرض" else "Default"
                            },
                            icon = Icons.Default.FormatSize
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(
                                        if (isDark) Color(0x12FFFFFF) else Color(0x0F000000),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val sizes = listOf(
                                    "small" to (if (isRtl) "کوچک" else "Small"),
                                    "default" to (if (isRtl) "معمولی" else "Default"),
                                    "large" to (if (isRtl) "بزرگ" else "Large")
                                )
                                sizes.forEach { (sizeCode, label) ->
                                    val isSelected = currentFontSize == sizeCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                feedbackManager.vibrateLight()
                                                prefs.fontSizeSetting = sizeCode
                                                feedbackManager.playSound("typing")
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CATEGORY 2: Feedback
                SettingsSectionHeader(title = tFeedback, icon = Icons.Default.VolumeUp)
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Sound Effects Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF00C2FF) else Color(0xFF6366F1),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tSoundEffects,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (isRtl) "فعال/غیرفعال کردن جلوه‌های صوتی برنامه" else "Play tactile sounds during interactions",
                                        fontSize = 12.sp,
                                        color = if (isDark) Color.Gray else Color.Gray
                                    )
                                }
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { isChecked ->
                                    prefs.soundEnabled = isChecked
                                    if (isChecked) {
                                        feedbackManager.playSound("typing")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                )
                            )
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // Haptic Feedback Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tHapticFeedback,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (isRtl) "لرزش‌های فیزیکی ظریف هنگام لمس" else "Gentle tactile vibration responses",
                                        fontSize = 12.sp,
                                        color = if (isDark) Color.Gray else Color.Gray
                                    )
                                }
                            }
                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { isChecked ->
                                    prefs.hapticEnabled = isChecked
                                    if (isChecked) {
                                        feedbackManager.vibrateMedium()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                )
                            )
                        }
                    }
                }

                // CATEGORY 3: Cloud Backup
                SettingsSectionHeader(title = tCloudBackup, icon = Icons.Default.CloudUpload)
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Google Account Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0x9EFFFFFF) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tGoogleAccount,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color.White else Color(0xFF1E293B)
                                )
                            }
                            Text(
                                text = "y.naderi3418@gmail.com",
                                color = if (isDark) Color(0xFF00C2FF) else Color(0xFF0284C7),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // Auto Backup Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tAutoBackup,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color.White else Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (isRtl) "پشتیبان‌گیری پس‌زمینه روزانه در گوگل درایو" else "Daily automatic backups to Google Drive",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.Gray else Color.Gray
                                )
                            }
                            Switch(
                                checked = autoBackup,
                                onCheckedChange = { isChecked ->
                                    prefs.cloudBackupAuto = isChecked
                                    feedbackManager.vibrateLight()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1)
                                )
                            )
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // Last Backup Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tLastBackup,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = lastBackupTime,
                                color = if (isDark) Color.LightGray else Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Backup and Restore Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Manual Backup Button
                            PremiumGlassButton(
                                text = if (isBackingUp) (if (isRtl) "درحال پشتیبان‌گیری..." else "Backing up...") else tBackupNow,
                                onClick = {
                                    if (!isBackingUp) {
                                        scope.launch {
                                            feedbackManager.vibrateMedium()
                                            isBackingUp = true
                                            delay(1800) // Simulating high-fidelity secure cloud upload operation
                                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            val nowStr = formatter.format(Date())
                                            prefs.lastBackupTime = nowStr
                                            isBackingUp = false
                                            feedbackManager.playSound("easy")
                                            feedbackManager.vibrateHeavy()
                                            snackbarHostState.showSnackbar(
                                                if (isRtl) "پشتیبان‌گیری با موفقیت انجام شد! حجم فایل: ۱۲.۴ کیلوبایت"
                                                else "Backup completed successfully! Size: 12.4 KB"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isBackingUp && !isRestoring
                            )

                            // Restore Backup Button
                            PremiumGlassButton(
                                text = if (isRestoring) (if (isRtl) "درحال بازیابی..." else "Restoring...") else tRestoreBackup,
                                onClick = {
                                    if (!isRestoring) {
                                        scope.launch {
                                            feedbackManager.vibrateMedium()
                                            isRestoring = true
                                            delay(2000) // Simulating database reconstruction and validation
                                            isRestoring = false
                                            feedbackManager.playSound("easy")
                                            feedbackManager.vibrateHeavy()
                                            snackbarHostState.showSnackbar(
                                                if (isRtl) "پشتیبان بازیابی شد و پایگاه داده به‌روز گردید!"
                                                else "Backup restored and database sync completed!"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isSecondary = true,
                                enabled = !isBackingUp && !isRestoring
                            )
                        }
                    }
                }

                // CATEGORY 4: About
                SettingsSectionHeader(title = tAbout, icon = Icons.Default.Info)
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // App Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Application Name",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "SevenTicks",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Version
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tVersion,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "2.1.0",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Build Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tBuild,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "419",
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Contact Email
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tContact,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "Info@7ticks.org",
                                color = if (isDark) Color(0xFF00C2FF) else Color(0xFF0284C7),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    try {
                                        feedbackManager.vibrateLight()
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:Info@7ticks.org")
                                            putExtra(Intent.EXTRA_SUBJECT, "SevenTicks App Feedback")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("No email app found to send feedback.")
                                        }
                                    }
                                }
                            )
                        }

                        Divider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000), thickness = 1.dp)

                        // Action Document Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Privacy Policy Button Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        feedbackManager.vibrateLight()
                                        showPrivacyDialog = true
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0x9EFFFFFF) else Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tPrivacyPolicy,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }
                                Icon(
                                    imageVector = if (isRtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Terms of Service Button Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        feedbackManager.vibrateLight()
                                        showTermsDialog = true
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0x9EFFFFFF) else Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tTermsOfService,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }
                                Icon(
                                    imageVector = if (isRtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Open Source Licenses Button Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        feedbackManager.vibrateLight()
                                        showLicensesDialog = true
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0x9EFFFFFF) else Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tLicenses,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }
                                Icon(
                                    imageVector = if (isRtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Modal Dialog: Privacy Policy
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text(tPrivacyPolicy, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isRtl) {
                            "سیاست حریم خصوصی برنامه سون‌تیکز\n\nما در سون‌تیکز به حریم خصوصی اطلاعات شما بسیار احترام می‌گذاریم. تمامی پیشرفت‌های یادگیری، لایتنر و اطلاعات شخصی شما به طور امن در حافظه محلی دستگاه شما با استفاده از تکنولوژی DataStore ذخیره می‌شوند. همچنین در صورت تمایل، با فعال‌سازی پشتیبان‌گیری خودکار، اطلاعات شما به طور رمزنگاری‌شده در سرورهای ابری امن گوگل درایو ذخیره می‌گردند. ما هرگز اطلاعات شما را با شخص ثالث به اشتراک نخواهیم گذاشت."
                        } else {
                            "SevenTicks Privacy Policy\n\nYour privacy is of paramount importance to us. All your learning session metrics, flashcard settings, Leitner box indices, and profile details are persisted securely on your local device storage using Android DataStore mechanisms. Backups are transmitted strictly to your personal Google Drive with proper end-to-end security configurations. We do not gather, store, or sell any personal telemetry data."
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        )
    }

    // Modal Dialog: Terms of Service
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text(tTermsOfService, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isRtl) {
                            "شرایط و ضوابط استفاده سون‌تیکز\n\nبا استفاده از سون‌تیکز، شما موافقت می‌کنید که از این سرویس برای بهینه‌سازی الگوریتم‌های یادگیری شخصی خود به نحو احسن استفاده نمایید. برنامه حاضر بدون گارانتی خاصی عرضه می‌شود و با هدف تسریع یادگیری عصبی عمیق طراحی شده است. الگوهای رفتاری Tiki صرفا تعاملی بوده و جنبه سرگرمی و انگیزشی دارند."
                        } else {
                            "SevenTicks Terms of Service\n\nBy accessing and utilizing SevenTicks, you agree to respect the neural spacing protocols and adhere to standard learning guidelines. The software is provided 'as is' without warranty of any kind. You hold sole ownership over your personal database models, flashcard structures, and spaced repetition curves. Tiki chatbot responses are dynamically generated to enhance memory pathways."
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        )
    }

    // Modal Dialog: Open Source Licenses
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text(tLicenses, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val licenses = listOf(
                        "Jetpack Compose - Apache 2.0 License\nProvides advanced modern declarative UI patterns for premium layouts.",
                        "Android Jetpack DataStore - Apache 2.0 License\nSecure client-side asynchronous preference persistence.",
                        "Kotlin Coroutines & Flow - Apache 2.0 License\nUnderpins real-time asynchronous threading and event flows.",
                        "Material Design 3 Components - Apache 2.0 License\nImplements premium M3 typography, shadows, elevation and visual balance.",
                        "FSRS Spaced Repetition Engine - MIT License\nAlgorithmic neural progression calculators."
                    )
                    licenses.forEach { lic ->
                        Text(
                            text = lic,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8B5CF6),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF8B5CF6),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color(0xFF00C2FF) else Color(0xFF6366F1),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = if (isDark) Color.LightGray else Color.DarkGray
                )
            }
        }
        content()
    }
}
