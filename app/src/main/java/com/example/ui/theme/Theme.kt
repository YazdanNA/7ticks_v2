package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Premium Cosmic Dark Color Scheme (SevenTicks Default)
private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF8B5CF6), // Futuristic violet
  secondary = Color(0xFF00C2FF), // Neon cyan
  tertiary = Color(0xFFEC4899), // Hot pink
  background = Color(0xFF060713), // Deep space black/navy
  surface = Color(0xFF111322), // Glassmorphic base card dark
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color(0xFFE2E8F0), // Slate gray text
  onSurface = Color.White,
  surfaceVariant = Color(0xFF1B1D30),
  outline = Color(0xFF2C2F48)
)

// Premium Balanced Light Color Scheme (Polished, non-washed out, elegant)
private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF6366F1), // Royal indigo
  secondary = Color(0xFF0284C7), // Sky blue accent
  tertiary = Color(0xFFDB2777), // Deep pink accent
  background = Color(0xFFF8FAFC), // Elegant soft gray/white canvas
  surface = Color(0xFFFFFFFF), // Pure white surface
  onPrimary = Color.White,
  onSecondary = Color.White,
  onBackground = Color(0xFF0F172A), // Midnight blue slate text
  onSurface = Color(0xFF1E293B),
  surfaceVariant = Color(0xFFF1F5F9), // Subtle light divider/tag base
  outline = Color(0xFFCBD5E1)
)

// Global custom composition local for app-wide language switching
val LocalAppLanguage = staticCompositionLocalOf { "en" }

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamicColor to false by default to preserve the premium custom-designed visual balances
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
