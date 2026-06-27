package com.example.core.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.features.analysis.presentation.AnalysisScreen
import com.example.features.boxes.presentation.BoxesScreen
import com.example.features.dictionary.presentation.DictionaryScreen
import com.example.features.onboarding.presentation.OnboardingWizardScreen
import com.example.features.profile.presentation.ProfileScreen
import com.example.features.smartlearn.presentation.LearningSessionScreen
import com.example.features.smartlearn.presentation.SmartLearnScreen
import com.example.features.splash.presentation.SplashScreen
import com.example.core.ui.components.AnimatedBackground
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingWizardScreen(navController = navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(
            route = Screen.LearningSession.route,
            arguments = listOf(
                navArgument("isBoxSession") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("boxId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val isBoxSession = backStackEntry.arguments?.getBoolean("isBoxSession") ?: false
            val boxId = backStackEntry.arguments?.getInt("boxId") ?: -1
            LearningSessionScreen(
                navController = navController,
                isBoxSession = isBoxSession,
                boxId = boxId
            )
        }
        composable(Screen.Dictionary.route) {
            // Accessible directly if navigated
            DictionaryScreen()
        }
    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavController) {
    var selectedTabRoute by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(TabScreen.SmartLearn.route) }
    var isBottomBarVisibleBySubScreen by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 5 }
    )

    // Instantly scroll to the target tab when the screen is first created/restored
    LaunchedEffect(Unit) {
        val targetPage = when (selectedTabRoute) {
            "smart_learn" -> 0
            "boxes" -> 1
            "dictionary" -> 2
            "analysis" -> 3
            "profile" -> 4
            else -> 0
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Update selectedTabRoute when the page settles (after swipe or programmatic scroll)
    LaunchedEffect(pagerState.settledPage) {
        val targetRoute = when (pagerState.settledPage) {
            0 -> "smart_learn"
            1 -> "boxes"
            2 -> "dictionary"
            3 -> "analysis"
            4 -> "profile"
            else -> "smart_learn"
        }
        if (selectedTabRoute != targetRoute) {
            selectedTabRoute = targetRoute
        }
    }

    LaunchedEffect(selectedTabRoute) {
        isBottomBarVisibleBySubScreen = true
    }

    val onTabClick: (String) -> Unit = { route ->
        val targetPage = when (route) {
            "smart_learn" -> 0
            "boxes" -> 1
            "dictionary" -> 2
            "analysis" -> 3
            "profile" -> 4
            else -> 0
        }
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            selectedTabRoute = route
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    page = targetPage,
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    val bottomBarVisible = if (selectedTabRoute == "dictionary") true else (selectedTabRoute != TabScreen.Boxes.route || isBottomBarVisibleBySubScreen)

    AnimatedBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content container (full screen with HorizontalPager)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    beyondViewportPageCount = 4
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> SmartLearnScreen(navController = navController)
                        1 -> BoxesScreen(
                            navController = navController,
                            onShowBottomBar = { isBottomBarVisibleBySubScreen = it }
                        )
                        2 -> DictionaryScreen()
                        3 -> AnalysisScreen()
                        4 -> ProfileScreen()
                    }
                }
            }

            // Glassmorphic Bottom Navigation as a floating overlay at the bottom
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x1F7A88FF))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0x3DFFFFFF), Color(0x127A88FF))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 1: Smart Learn
                        BottomNavItem(
                            title = "Smart Learn",
                            icon = Icons.Default.Star,
                            active = selectedTabRoute == "smart_learn",
                            onClick = {
                                onTabClick("smart_learn")
                            }
                        )

                        // Tab 2: Boxes
                        BottomNavItem(
                            title = "Boxes",
                            icon = Icons.Default.List,
                            active = selectedTabRoute == "boxes",
                            onClick = {
                                onTabClick("boxes")
                            }
                        )

                        // Center Quick Toggle: Dictionary
                        val isDictionaryActive = selectedTabRoute == "dictionary"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onTabClick("dictionary") }
                                .padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDictionaryActive) {
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF00C2FF), Color(0xFF9D00FF))
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0x22FFFFFF), Color(0x0DFFFFFF))
                                            )
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isDictionaryActive) Color.White else Color(0x33FFFFFF),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Dictionary Toggle",
                                    tint = if (isDictionaryActive) Color.White else Color(0xFF00FFD2),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dictionary",
                                color = if (isDictionaryActive) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Tab 3: Analysis
                        BottomNavItem(
                            title = "Analysis",
                            icon = Icons.Default.PlayArrow, // Chart-like representation
                            active = selectedTabRoute == "analysis",
                            onClick = {
                                onTabClick("analysis")
                            }
                        )

                        // Tab 4: Profile
                        BottomNavItem(
                            title = "Profile",
                            icon = Icons.Default.Person,
                            active = selectedTabRoute == "profile",
                            onClick = {
                                onTabClick("profile")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (active) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (active) Color(0xFF00FFD2) else Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}
