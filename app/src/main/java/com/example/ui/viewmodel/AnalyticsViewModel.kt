package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.NoteMastery
import com.example.data.database.SessionRecord
import com.example.data.database.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val sessions: List<SessionRecord> = emptyList(),
    val noteMasteryList: List<NoteMastery> = emptyList(),
    val totalSessionsPlayed: Int = 0,
    val overallAccuracyPct: Float = 0f,
    val averageReactionTimeMs: Long = 0L,
    val highestScore: Int = 0,
    val longestStreak: Int = 0,
    val strongestNote: String = "N/A",
    val weakestNote: String = "N/A"
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepository(AppDatabase.getDatabase(application).sessionDao())

    val uiState: StateFlow<AnalyticsUiState> = repository.allSessions.map { sessions ->
        val masteryList = repository.allNoteMastery.map { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList()).value

        val totalSessions = sessions.size
        val totalQuestions = sessions.sumOf { it.totalQuestions }
        val totalCorrect = sessions.sumOf { (it.accuracyPercentage * it.totalQuestions / 100f).toInt() }
        val overallAccuracy = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions) * 100f else 0f
        
        val validReactionTimes = sessions.filter { it.avgReactionTimeMs > 0 }
        val avgReactionTime = if (validReactionTimes.isNotEmpty()) validReactionTimes.map { it.avgReactionTimeMs }.average().toLong() else 0L
        
        val maxScore = sessions.maxOfOrNull { it.score } ?: 0
        val maxStreak = sessions.maxOfOrNull { it.maxStreak } ?: 0

        AnalyticsUiState(
            sessions = sessions,
            noteMasteryList = masteryList,
            totalSessionsPlayed = totalSessions,
            overallAccuracyPct = overallAccuracy,
            averageReactionTimeMs = avgReactionTime,
            highestScore = maxScore,
            longestStreak = maxStreak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
