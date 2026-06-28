package com.example.features.tiki.relationship

data class RelationshipSnapshot(
    val level: Int,
    val levelName: String,
    val xp: Int,
    val xpToNextLevel: Int,
    val progressToNextLevel: Float,
    val activityFamiliarity: Float,
    val progress: RelationshipProgress
)
