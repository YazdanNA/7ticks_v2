package com.example.features.tiki.coaching

import com.example.features.tiki.director.DirectorEngine
import com.example.features.tiki.director.DirectorPriority
import com.example.features.tiki.director.DirectorState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class CoachingEngineTest {

    private lateinit var goalRepository: GoalRepository
    private lateinit var directorEngine: DirectorEngine
    private lateinit var coachingEngine: CoachingEngine

    @Before
    fun setUp() {
        // Reset singleton of DirectorEngine to a clean state
        directorEngine = DirectorEngine.getInstance()
        directorEngine.reset()

        goalRepository = GoalRepository()
        coachingEngine = CoachingEngine(goalRepository, directorEngine)
    }

    @Test
    fun `test default goals are correctly registered`() {
        val goals = goalRepository.getGoals()
        
        // Assert that the default goals are all present
        assertTrue(goals.any { it is LearningGoal.DailyStudy })
        assertTrue(goals.any { it is LearningGoal.DailyReview })
        assertTrue(goals.any { it is LearningGoal.DailyNewWords })
        assertTrue(goals.any { it is LearningGoal.WeeklyConsistency })
        assertTrue(goals.any { it is LearningGoal.MasterWords })
        assertTrue(goals.any { it is LearningGoal.LongestStreak })
        assertTrue(goals.any { it is LearningGoal.SessionCompletion })
        assertTrue(goals.any { it is LearningGoal.ReviewQueueCleanup })
    }

    @Test
    fun `test goal tracking and update progress`() {
        val progressBefore = goalRepository.getProgress("daily_study")
        assertNotNull(progressBefore)
        assertEquals(0f, progressBefore!!.currentValue)
        assertFalse(progressBefore.isCompleted)

        // Update progress to halfway
        goalRepository.updateProgress("daily_study", 10f)
        val progressHalfway = goalRepository.getProgress("daily_study")
        assertNotNull(progressHalfway)
        assertEquals(10f, progressHalfway!!.currentValue)
        assertFalse(progressHalfway.isCompleted)

        // Update progress to completion
        goalRepository.updateProgress("daily_study", 15f)
        val progressComplete = goalRepository.getProgress("daily_study")
        assertNotNull(progressComplete)
        assertEquals(15f, progressComplete!!.currentValue)
        assertTrue(progressComplete.isCompleted)
    }

    @Test
    fun `test suggestion timing - no interruptions while studying`() {
        coachingEngine.setStudyingState(true) // User is studying!
        
        val context = CoachingContext(
            progresses = goalRepository.getProgresses(),
            remainingReviews = 20,
            studyDurationMinutes = 5,
            currentStreak = 4,
            recentPerformanceScore = 0.9f,
            relationshipLevel = 2,
            timing = SuggestionTiming.BEFORE_SESSION,
            preferredStyle = CoachingStyle.Encouraging
        )

        val suggestions = coachingEngine.getSuggestions(context)
        
        // Should return zero suggestions during active study to avoid interruptions
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `test suggestion timing - safe timing allows suggestions`() {
        coachingEngine.setStudyingState(false) // User is NOT studying
        
        val context = CoachingContext(
            progresses = goalRepository.getProgresses(),
            remainingReviews = 20,
            studyDurationMinutes = 5,
            currentStreak = 4,
            recentPerformanceScore = 0.9f,
            relationshipLevel = 2,
            timing = SuggestionTiming.BEFORE_SESSION,
            preferredStyle = CoachingStyle.Encouraging
        )

        val suggestions = coachingEngine.getSuggestions(context)
        
        // Should have suggestions for starting session and reviews
        assertFalse(suggestions.isEmpty())
        assertTrue(suggestions.any { it.actionType == CoachingActionType.START_TODAY_SESSION })
    }

    @Test
    fun `test motivation consistency for different styles`() {
        coachingEngine.setStudyingState(false)

        val contextBase = CoachingContext(
            progresses = goalRepository.getProgresses(),
            remainingReviews = 20,
            studyDurationMinutes = 5,
            currentStreak = 4,
            recentPerformanceScore = 0.9f,
            relationshipLevel = 2,
            timing = SuggestionTiming.BEFORE_SESSION
        )

        // 1. Encouraging
        val encouragingSugs = coachingEngine.getSuggestions(contextBase.copy(preferredStyle = CoachingStyle.Encouraging))
        val studyEnc = encouragingSugs.first { it.id == "start_session" }
        assertTrue(studyEnc.text.contains("kick off") || studyEnc.text.contains("progress"))

        // 2. Gentle
        val gentleSugs = coachingEngine.getSuggestions(contextBase.copy(preferredStyle = CoachingStyle.Gentle))
        val studyGen = gentleSugs.first { it.id == "start_session" }
        assertTrue(studyGen.text.contains("Whenever") || studyGen.text.contains("own pace"))

        // 3. Playful
        val playfulSugs = coachingEngine.getSuggestions(contextBase.copy(preferredStyle = CoachingStyle.Playful))
        val studyPlay = playfulSugs.first { it.id == "start_session" }
        assertTrue(studyPlay.text.contains("Ready") || studyPlay.text.contains("brain"))

        // 4. Calm
        val calmSugs = coachingEngine.getSuggestions(contextBase.copy(preferredStyle = CoachingStyle.Calm))
        val studyCalm = calmSugs.first { it.id == "start_session" }
        assertTrue(studyCalm.text.contains("peaceful") || studyCalm.text.contains("moment"))
    }

    @Test
    fun `test director engine receives achievement on goal completion`() {
        var eventReceived = false
        directorEngine.addDecisionListener { decision ->
            eventReceived = true
        }

        // Trigger goal completion
        val goal = LearningGoal.DailyStudy(targetMinutes = 15)
        coachingEngine.onGoalCompleted(goal, random = Random(0)) // deterministic random

        // Check if director engine received the event and updated state correctly
        assertTrue(eventReceived)
        
        val activePriority = directorEngine.state.value.activePriority
        assertEquals(DirectorPriority.ACHIEVEMENT.value, activePriority)
    }

    @Test
    fun `test future goals extensibility`() {
        // Create a custom goal (e.g. Monthly Goal or Personal Goal)
        val monthlyReadingGoal = LearningGoal.CustomGoal(
            id = "monthly_reading_50",
            title = "Read 50 Pages",
            description = "Read at least 50 pages of target language material",
            targetValue = 50f
        )

        // Register custom goal without engine rewrite
        goalRepository.registerGoal(monthlyReadingGoal)

        // Retrieve custom goal and progress
        val retrieved = goalRepository.getGoal("monthly_reading_50")
        assertNotNull(retrieved)
        assertEquals("Read 50 Pages", retrieved!!.title)

        val progress = goalRepository.getProgress("monthly_reading_50")
        assertNotNull(progress)
        assertEquals(50f, progress!!.targetValue)
        assertEquals(0f, progress.currentValue)

        // Update progress
        goalRepository.updateProgress("monthly_reading_50", 50f)
        assertTrue(goalRepository.getProgress("monthly_reading_50")!!.isCompleted)
    }
}
