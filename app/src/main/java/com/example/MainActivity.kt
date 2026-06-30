package com.example

import android.os.Bundle
import android.os.Build
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.core.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LocalAppLanguage

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val prefs = remember { SevenTicksApplication.instance.preferencesManager }
      
      val themeMode by prefs.themeModeFlow.collectAsState(initial = prefs.themeMode)
      val appLanguage by prefs.appLanguageFlow.collectAsState(initial = prefs.appLanguage)
      val fontSizeSetting by prefs.fontSizeFlow.collectAsState(initial = prefs.fontSizeSetting)
      
      // Update locale immediately
      LaunchedEffect(appLanguage) {
        try {
          val locale = java.util.Locale(appLanguage)
          java.util.Locale.setDefault(locale)
          val resources = context.resources
          val configuration = resources.configuration
          if (configuration.locales[0] != locale) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? android.app.LocaleManager
              localeManager?.applicationLocales = android.os.LocaleList(locale)
            } else {
              @Suppress("DEPRECATION")
              resources.updateConfiguration(configuration, resources.displayMetrics)
            }
          }
        } catch (e: Exception) {
          // Handle gracefully
        }
      }

      // Determine dark theme
      val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
      }

      // Determine font scale
      val fontScale = when (fontSizeSetting) {
        "small" -> 0.85f
        "large" -> 1.15f
        else -> 1.0f
      }

      val currentDensity = LocalDensity.current
      val customDensity = remember(currentDensity, fontScale) {
        Density(
          density = currentDensity.density,
          fontScale = currentDensity.fontScale * fontScale
        )
      }

      MyApplicationTheme(darkTheme = isDark) {
        CompositionLocalProvider(
          LocalDensity provides customDensity,
          LocalAppLanguage provides appLanguage
        ) {
          AppNavigation()
        }
      }
    }
  }
}

