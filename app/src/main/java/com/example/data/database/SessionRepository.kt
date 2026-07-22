package com.example.data.database

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<SessionRecord>> = sessionDao.getAllSessions()
    val allNoteMastery: Flow<List<NoteMastery>> = sessionDao.getAllNoteMastery()

    suspend fun saveSession(
        modeName: String,
        score: Int,
        totalQuestions: Int,
        avgReactionTimeMs: Long,
        maxStreak: Int
    ): Long {
        val accuracyPct = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100f else 0f
        val record = SessionRecord(
            modeName = modeName,
            score = score,
            totalQuestions = totalQuestions,
            accuracyPercentage = accuracyPct,
            avgReactionTimeMs = avgReactionTimeMs,
            maxStreak = maxStreak
        )
        return sessionDao.insertSession(record)
    }

    suspend fun recordAttempt(noteKey: String, isCorrect: Boolean, timeTakenMs: Long) {
        val existing = sessionDao.getMasteryForKey(noteKey) ?: NoteMastery(noteKey = noteKey)
        val updated = existing.copy(
            attempts = existing.attempts + 1,
            correctCount = existing.correctCount + (if (isCorrect) 1 else 0),
            totalTimeMs = existing.totalTimeMs + timeTakenMs
        )
        sessionDao.insertOrUpdateMastery(updated)
    }

    suspend fun clearAllHistory() {
        sessionDao.clearHistory()
    }
}
