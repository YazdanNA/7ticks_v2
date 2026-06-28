package com.example.features.tiki.coaching

data class GoalProgress(
    val goalId: String,
    val currentValue: Float,
    val targetValue: Float,
    val isCompleted: Boolean = currentValue >= targetValue
) {
    val percentage: Float
        get() = if (targetValue > 0f) (currentValue / targetValue).coerceIn(0f, 1f) else 0f
}
