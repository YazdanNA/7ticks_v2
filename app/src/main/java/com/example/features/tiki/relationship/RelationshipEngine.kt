package com.example.features.tiki.relationship

import android.content.Context
import com.example.features.tiki.dialogue.DialogueCategory

class RelationshipEngine(
    val repository: RelationshipRepository = RelationshipRepository()
) {
    fun onEvent(
        event: RelationshipEvent,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): RelationshipSnapshot {
        val currentProgress = repository.getProgress()
        val xpGain = RelationshipRules.calculateXpGain(event)
        val newXp = currentProgress.xp + xpGain

        var newCompletedSessions = currentProgress.completedSessions
        var newTotalStudyTime = currentProgress.totalStudyTimeMillis
        var newAvgSessionLength = currentProgress.averageSessionLengthSeconds
        var newStudyDays = currentProgress.studyDays
        var newMasterWords = currentProgress.masterWords
        var newCurrentStreak = currentProgress.currentStreak
        var newLongestStreak = currentProgress.longestStreak

        when (event) {
            is RelationshipEvent.SessionCompleted -> {
                newCompletedSessions++
                newTotalStudyTime += event.durationMillis
                if (newCompletedSessions > 0) {
                    newAvgSessionLength = (newTotalStudyTime / 1000L) / newCompletedSessions
                }
            }
            is RelationshipEvent.DailyStudyPlayed -> {
                newStudyDays++
                newTotalStudyTime += 600000L // 10 minutes session estimate
            }
            is RelationshipEvent.ReturningTomorrow -> {
                newStudyDays++
            }
            is RelationshipEvent.WordsMastered -> {
                newMasterWords += event.count
            }
            is RelationshipEvent.StreakMaintained -> {
                newCurrentStreak = event.days
                newLongestStreak = maxOf(newLongestStreak, event.days)
            }
            is RelationshipEvent.DifficultWordHelped -> {
                // Just grants XP and counts towards active participation
            }
            else -> {
                // Prepared/Future support
            }
        }

        val updatedProgress = currentProgress.copy(
            xp = newXp,
            lastActivityTimeMillis = currentTimeMillis,
            completedSessions = newCompletedSessions,
            totalStudyTimeMillis = newTotalStudyTime,
            averageSessionLengthSeconds = newAvgSessionLength,
            studyDays = newStudyDays,
            masterWords = newMasterWords,
            currentStreak = newCurrentStreak,
            longestStreak = newLongestStreak
        )

        repository.saveProgress(updatedProgress)
        return getSnapshot(currentTimeMillis)
    }

    fun getSnapshot(currentTimeMillis: Long = System.currentTimeMillis()): RelationshipSnapshot {
        val progress = repository.getProgress()
        val currentLevel = RelationshipLevel.getLevelForXp(progress.xp)
        val nextLevelNumber = (currentLevel.levelNumber + 1).coerceAtMost(RelationshipLevel.configurableLevels.size)
        val nextLevel = RelationshipLevel.getLevelByNumber(nextLevelNumber)

        val xpToNextLevel = if (currentLevel.levelNumber == nextLevel.levelNumber) {
            0
        } else {
            (nextLevel.xpRequired - progress.xp).coerceAtLeast(0)
        }

        val progressToNextLevel = if (currentLevel.levelNumber == nextLevel.levelNumber) {
            1.0f
        } else {
            val range = nextLevel.xpRequired - currentLevel.xpRequired
            val earned = progress.xp - currentLevel.xpRequired
            if (range > 0) {
                (earned.toFloat() / range.toFloat()).coerceIn(0.0f, 1.0f)
            } else {
                1.0f
            }
        }

        // Decay logic for activity familiarity
        val activityFamiliarity = if (progress.lastActivityTimeMillis == 0L) {
            1.0f
        } else {
            val daysElapsed = ((currentTimeMillis - progress.lastActivityTimeMillis) / (24 * 3600 * 1000L)).coerceAtLeast(0L)
            if (daysElapsed <= 1L) {
                1.0f
            } else {
                (1.0f - (daysElapsed - 1) * 0.1f).coerceIn(0.1f, 1.0f)
            }
        }

        return RelationshipSnapshot(
            level = currentLevel.levelNumber,
            levelName = currentLevel.name,
            xp = progress.xp,
            xpToNextLevel = xpToNextLevel,
            progressToNextLevel = progressToNextLevel,
            activityFamiliarity = activityFamiliarity,
            progress = progress
        )
    }

    fun modifyDialogue(
        baseText: String,
        category: DialogueCategory,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): String {
        val snapshot = getSnapshot(currentTimeMillis)
        return RelationshipRules.modifyDialogue(baseText, snapshot, category)
    }

    fun reset() {
        repository.clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: RelationshipEngine? = null

        fun getInstance(context: Context? = null): RelationshipEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = RelationshipEngine(RelationshipRepository(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
