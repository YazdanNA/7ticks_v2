package com.example.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object LearningSession : Screen("learning_session?isBoxSession={isBoxSession}&boxId={boxId}") {
        fun createRoute(isBoxSession: Boolean, boxId: Int): String {
            return "learning_session?isBoxSession=$isBoxSession&boxId=$boxId"
        }
    }
    object Dictionary : Screen("dictionary")
}

sealed class TabScreen(val route: String, val title: String) {
    object SmartLearn : TabScreen("smart_learn", "Smart Learn")
    object Boxes : TabScreen("boxes", "Boxes")
    object Analysis : TabScreen("analysis", "Analysis")
    object Profile : TabScreen("profile", "Profile")
}
