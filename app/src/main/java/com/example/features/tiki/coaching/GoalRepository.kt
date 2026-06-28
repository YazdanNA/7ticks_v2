package com.example.features.tiki.coaching

import java.util.concurrent.ConcurrentHashMap

class GoalRepository {
    private val goals = ConcurrentHashMap<String, LearningGoal>()
    private val progresses = ConcurrentHashMap<String, GoalProgress>()
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<(GoalProgress) -> Unit>()
    private val goalCompletedListeners = java.util.concurrent.CopyOnWriteArrayList<(LearningGoal) -> Unit>()

    fun registerGoal(goal: LearningGoal) {
        goals[goal.id] = goal
        // Initialize progress if it doesn't exist
        val target = when (goal) {
            is LearningGoal.DailyStudy -> goal.targetMinutes.toFloat()
            is LearningGoal.DailyReview -> goal.targetReviewCount.toFloat()
            is LearningGoal.DailyNewWords -> goal.targetNewWords.toFloat()
            is LearningGoal.WeeklyConsistency -> goal.targetDays.toFloat()
            is LearningGoal.MasterWords -> goal.targetMasteredWordsCount.toFloat()
            is LearningGoal.LongestStreak -> goal.targetStreakDays.toFloat()
            is LearningGoal.SessionCompletion -> goal.targetSessionsCount.toFloat()
            is LearningGoal.ReviewQueueCleanup -> goal.targetQueueSize.toFloat()
            is LearningGoal.CustomGoal -> goal.targetValue
        }
        if (!progresses.containsKey(goal.id)) {
            progresses[goal.id] = GoalProgress(goal.id, 0f, target)
        }
    }

    fun getGoals(): List<LearningGoal> = goals.values.toList()

    fun getGoal(id: String): LearningGoal? = goals[id]

    fun getProgress(id: String): GoalProgress? = progresses[id]

    fun getProgresses(): Map<String, GoalProgress> = progresses.toMap()

    fun updateProgress(goalId: String, newValue: Float) {
        val existing = progresses[goalId] ?: return
        val updated = existing.copy(
            currentValue = newValue,
            isCompleted = newValue >= existing.targetValue
        )
        progresses[goalId] = updated
        listeners.forEach { it(updated) }
        
        if (updated.isCompleted && !existing.isCompleted) {
            val goal = goals[goalId]
            if (goal != null) {
                goalCompletedListeners.forEach { it(goal) }
            }
        }
    }

    fun addProgressChangeListener(listener: (GoalProgress) -> Unit) {
        listeners.add(listener)
    }

    fun addGoalCompletedListener(listener: (LearningGoal) -> Unit) {
        goalCompletedListeners.add(listener)
    }

    fun clear() {
        goals.clear()
        progresses.clear()
        listeners.clear()
        goalCompletedListeners.clear()
    }
}
