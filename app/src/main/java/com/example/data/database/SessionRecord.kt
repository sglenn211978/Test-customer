package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeName: String, // e.g. "Note Identification", "Find Note", "Ear Training"
    val score: Int,
    val totalQuestions: Int,
    val accuracyPercentage: Float,
    val avgReactionTimeMs: Long,
    val maxStreak: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_mastery")
data class NoteMastery(
    @PrimaryKey val noteKey: String, // e.g. "C", "C#", "String1_Fret3"
    val attempts: Int = 0,
    val correctCount: Int = 0,
    val totalTimeMs: Long = 0
) {
    val accuracyPct: Float
        get() = if (attempts > 0) (correctCount.toFloat() / attempts.toFloat()) * 100f else 0f

    val avgTimeMs: Long
        get() = if (attempts > 0) totalTimeMs / attempts else 0L
}
