package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GuitarAudioEngine
import com.example.data.database.AppDatabase
import com.example.data.database.SessionRepository
import com.example.data.model.FretPosition
import com.example.data.model.GuitarString
import com.example.data.model.IntervalType
import com.example.data.model.NoteName
import com.example.data.model.StandardTuning
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random

enum class QuizDifficulty(val label: String, val maxFret: Int, val naturalNotesOnly: Boolean, val timerSeconds: Int) {
    BEGINNER("Beginner (Frets 0-3, Natural Notes)", 3, true, 18),
    INTERMEDIATE("Intermediate (Frets 0-5)", 5, false, 12),
    PRO("Pro (Frets 0-12, Full Neck)", 12, false, 7)
}

enum class QuizType(val title: String, val description: String) {
    NOTE_IDENTIFICATION("Note Identification", "Name the highlighted position under the timer!"),
    FIND_THE_NOTE("Find The Note", "Tap all occurrences of the target note across the neck!"),
    EAR_TRAINING("Ear Training", "Listen to the note/interval pitch and identify it!")
}

data class QuizUiState(
    val quizType: QuizType = QuizType.NOTE_IDENTIFICATION,
    val difficulty: QuizDifficulty = QuizDifficulty.BEGINNER,
    val isActive: Boolean = false,
    val isFinished: Boolean = false,
    
    // Game metrics
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 10,
    val timeLeftSeconds: Int = 18,
    val maxTimeSeconds: Int = 18,
    val avgReactionTimeMs: Long = 0L,
    val lastResponseTimeMs: Long = 0L,
    val correctAnswersCount: Int = 0,
    
    // Note ID state
    val targetPosition: FretPosition? = null,
    val targetNote: NoteName? = null,
    val optionChoices: List<NoteName> = emptyList(),
    val selectedChoice: NoteName? = null,
    val isAnswerCorrect: Boolean? = null,
    val beginnerHint: String? = null,
    
    // Find The Note state
    val findTargetNote: NoteName? = null,
    val remainingTargetPositions: Set<FretPosition> = emptySet(),
    val foundPositions: Set<FretPosition> = emptySet(),
    val incorrectTappedPositions: Set<FretPosition> = emptySet(),
    
    // Ear Training state
    val earTargetInterval: IntervalType? = null,
    val earRootNote: NoteName? = null,
    val optionIntervals: List<IntervalType> = emptyList(),
    
    // Audio engine reference
    val strings: List<GuitarString> = StandardTuning.STRINGS
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepository(AppDatabase.getDatabase(application).sessionDao())
    private val audioEngine = GuitarAudioEngine()
    private val random = Random()

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTimeMs: Long = 0
    private var totalReactionTimeMs: Long = 0

    fun setDifficulty(difficulty: QuizDifficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun startQuiz(quizType: QuizType, totalQuestions: Int = 10, difficulty: QuizDifficulty = _uiState.value.difficulty) {
        _uiState.update {
            QuizUiState(
                quizType = quizType,
                difficulty = difficulty,
                isActive = true,
                isFinished = false,
                score = 0,
                streak = 0,
                maxStreak = 0,
                questionNumber = 0,
                totalQuestions = totalQuestions,
                strings = StandardTuning.STRINGS
            )
        }
        totalReactionTimeMs = 0
        nextQuestion()
    }

    private fun startTimer() {
        timerJob?.cancel()
        val timeForType = _uiState.value.difficulty.timerSeconds

        _uiState.update { it.copy(timeLeftSeconds = timeForType, maxTimeSeconds = timeForType) }
        questionStartTimeMs = System.currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSeconds > 0 && _uiState.value.isActive && _uiState.value.isAnswerCorrect == null) {
                delay(1000)
                _uiState.update { current ->
                    val newTime = current.timeLeftSeconds - 1
                    current.copy(timeLeftSeconds = newTime)
                }
            }

            if (_uiState.value.timeLeftSeconds <= 0 && _uiState.value.isAnswerCorrect == null) {
                // Time expired!
                handleTimeExpired()
            }
        }
    }

    fun replayEarTrainingAudio() {
        val state = _uiState.value
        when (state.quizType) {
            QuizType.EAR_TRAINING -> {
                state.targetPosition?.let { pos ->
                    audioEngine.playPluckedNote(pos.getFrequencyHz(state.strings))
                }
                if (state.earTargetInterval != null && state.earRootNote != null) {
                    val rootFreq = FretPosition(5, 0).getFrequencyHz(state.strings)
                    val targetFreq = rootFreq * Math.pow(2.0, state.earTargetInterval.semitones / 12.0).toFloat()
                    audioEngine.playArpeggio(listOf(rootFreq, targetFreq), 400)
                }
            }
            QuizType.NOTE_IDENTIFICATION -> {
                state.targetPosition?.let { pos ->
                    audioEngine.playPluckedNote(pos.getFrequencyHz(state.strings))
                }
            }
            else -> {}
        }
    }

    private fun nextQuestion() {
        val current = _uiState.value
        if (current.questionNumber >= current.totalQuestions) {
            finishQuiz()
            return
        }

        val nextQNum = current.questionNumber + 1
        val diff = current.difficulty

        when (current.quizType) {
            QuizType.NOTE_IDENTIFICATION -> {
                var stringNum: Int
                var fretNum: Int
                var pos: FretPosition
                var targetNote: NoteName

                // Generate note based on difficulty
                val naturalNotes = listOf(NoteName.C, NoteName.D, NoteName.E, NoteName.F, NoteName.G, NoteName.A, NoteName.B)
                do {
                    stringNum = random.nextInt(6) + 1
                    fretNum = random.nextInt(diff.maxFret + 1)
                    pos = FretPosition(stringNum, fretNum)
                    targetNote = pos.getNoteName(current.strings)
                } while (diff.naturalNotesOnly && !naturalNotes.contains(targetNote))

                val optionPool = if (diff.naturalNotesOnly) naturalNotes else NoteName.ALL_NOTES.toList()
                val numOptions = if (diff.naturalNotesOnly) 3 else 4

                val options = mutableSetOf(targetNote)
                while (options.size < numOptions) {
                    options.add(optionPool[random.nextInt(optionPool.size)])
                }

                val hintText = if (fretNum == 0) "Open string ${pos.stringNumber} (${current.strings.find { it.stringNumber == pos.stringNumber }?.openNote?.displayName})"
                else "String ${pos.stringNumber}, Fret $fretNum"

                _uiState.update {
                    it.copy(
                        questionNumber = nextQNum,
                        targetPosition = pos,
                        targetNote = targetNote,
                        optionChoices = options.toList().shuffled(),
                        selectedChoice = null,
                        isAnswerCorrect = null,
                        beginnerHint = hintText
                    )
                }
                // Play audio cue
                audioEngine.playPluckedNote(pos.getFrequencyHz(current.strings))
            }

            QuizType.FIND_THE_NOTE -> {
                val naturalNotes = listOf(NoteName.C, NoteName.D, NoteName.E, NoteName.F, NoteName.G, NoteName.A, NoteName.B)
                val targetNote = if (diff.naturalNotesOnly) naturalNotes[random.nextInt(naturalNotes.size)] else NoteName.ALL_NOTES[random.nextInt(12)]
                val matchingPositions = mutableSetOf<FretPosition>()

                for (str in current.strings) {
                    for (fret in 0..diff.maxFret) {
                        val pos = FretPosition(str.stringNumber, fret)
                        if (pos.getNoteName(current.strings) == targetNote) {
                            matchingPositions.add(pos)
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        questionNumber = nextQNum,
                        findTargetNote = targetNote,
                        remainingTargetPositions = matchingPositions,
                        foundPositions = emptySet(),
                        incorrectTappedPositions = emptySet(),
                        isAnswerCorrect = null,
                        beginnerHint = "Find all ${targetNote.displayName} notes between frets 0 and ${diff.maxFret}"
                    )
                }
            }

            QuizType.EAR_TRAINING -> {
                val intervals = if (diff == QuizDifficulty.BEGINNER) {
                    arrayOf(IntervalType.UNISON, IntervalType.MAJOR_3RD, IntervalType.PERFECT_5TH, IntervalType.OCTAVE)
                } else {
                    IntervalType.entries.toTypedArray()
                }
                val targetInterval = intervals[random.nextInt(intervals.size)]
                val rootNote = NoteName.A

                val options = mutableSetOf(targetInterval)
                val optionCount = if (diff == QuizDifficulty.BEGINNER) 3 else 4
                while (options.size < optionCount) {
                    options.add(intervals[random.nextInt(intervals.size)])
                }

                _uiState.update {
                    it.copy(
                        questionNumber = nextQNum,
                        earRootNote = rootNote,
                        earTargetInterval = targetInterval,
                        optionIntervals = options.toList().shuffled(),
                        selectedChoice = null,
                        isAnswerCorrect = null,
                        beginnerHint = "Listen for ${targetInterval.displayName}"
                    )
                }

                val rootFreq = 220.0f
                val targetFreq = rootFreq * Math.pow(2.0, targetInterval.semitones / 12.0).toFloat()
                audioEngine.playArpeggio(listOf(rootFreq, targetFreq), 400)
            }
        }

        startTimer()
    }

    fun submitNoteIdAnswer(chosenNote: NoteName) {
        val state = _uiState.value
        if (state.isAnswerCorrect != null || state.targetNote == null) return

        timerJob?.cancel()
        val timeTakenMs = System.currentTimeMillis() - questionStartTimeMs
        totalReactionTimeMs += timeTakenMs

        val isCorrect = (chosenNote == state.targetNote)
        val newStreak = if (isCorrect) state.streak + 1 else 0
        val newMaxStreak = maxOf(state.maxStreak, newStreak)
        val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        
        // Speed bonus calculation: faster answers yield extra bonus points!
        val speedBonus = if (isCorrect) maxOf(0, (state.maxTimeSeconds * 1000 - timeTakenMs).toInt() / 100) else 0
        val newScore = state.score + (if (isCorrect) 100 + (newStreak * 15) + speedBonus else 0)

        _uiState.update {
            it.copy(
                selectedChoice = chosenNote,
                isAnswerCorrect = isCorrect,
                score = newScore,
                streak = newStreak,
                maxStreak = newMaxStreak,
                lastResponseTimeMs = timeTakenMs,
                correctAnswersCount = newCorrectCount
            )
        }

        // Play audio confirmation
        state.targetPosition?.let { pos ->
            audioEngine.playPluckedNote(pos.getFrequencyHz(state.strings))
        }

        // Record attempt to DB
        viewModelScope.launch {
            repository.recordAttempt(state.targetNote.displayName, isCorrect, timeTakenMs)
        }

        // Delay then proceed
        viewModelScope.launch {
            delay(1200)
            nextQuestion()
        }
    }

    fun submitFindNoteTap(pos: FretPosition) {
        val state = _uiState.value
        if (state.isAnswerCorrect != null || state.findTargetNote == null) return

        val freq = pos.getFrequencyHz(state.strings)
        audioEngine.playPluckedNote(freq)

        val noteAtPos = pos.getNoteName(state.strings)
        val timeTakenMs = System.currentTimeMillis() - questionStartTimeMs

        if (noteAtPos == state.findTargetNote) {
            val updatedRemaining = state.remainingTargetPositions - pos
            val updatedFound = state.foundPositions + pos

            _uiState.update {
                it.copy(
                    remainingTargetPositions = updatedRemaining,
                    foundPositions = updatedFound
                )
            }

            viewModelScope.launch {
                repository.recordAttempt("Pos_${pos.stringNumber}_${pos.fretNumber}", true, timeTakenMs)
            }

            if (updatedRemaining.isEmpty()) {
                // Found all targets!
                timerJob?.cancel()
                val newStreak = state.streak + 1
                val newMaxStreak = maxOf(state.maxStreak, newStreak)
                val newScore = state.score + 200 + (newStreak * 20)

                _uiState.update {
                    it.copy(
                        isAnswerCorrect = true,
                        score = newScore,
                        streak = newStreak,
                        maxStreak = newMaxStreak
                    )
                }

                viewModelScope.launch {
                    delay(1200)
                    nextQuestion()
                }
            }
        } else {
            // Incorrect tap
            val updatedIncorrect = state.incorrectTappedPositions + pos
            _uiState.update {
                it.copy(
                    incorrectTappedPositions = updatedIncorrect,
                    streak = 0
                )
            }
            viewModelScope.launch {
                repository.recordAttempt("Pos_${pos.stringNumber}_${pos.fretNumber}", false, timeTakenMs)
            }
        }
    }

    fun submitEarTrainingAnswer(chosenInterval: IntervalType) {
        val state = _uiState.value
        if (state.isAnswerCorrect != null || state.earTargetInterval == null) return

        timerJob?.cancel()
        val timeTakenMs = System.currentTimeMillis() - questionStartTimeMs
        totalReactionTimeMs += timeTakenMs

        val isCorrect = (chosenInterval == state.earTargetInterval)
        val newStreak = if (isCorrect) state.streak + 1 else 0
        val newMaxStreak = maxOf(state.maxStreak, newStreak)
        val newScore = state.score + (if (isCorrect) 150 + (newStreak * 15) else 0)

        _uiState.update {
            it.copy(
                isAnswerCorrect = isCorrect,
                score = newScore,
                streak = newStreak,
                maxStreak = newMaxStreak
            )
        }

        viewModelScope.launch {
            repository.recordAttempt(state.earTargetInterval.displayName, isCorrect, timeTakenMs)
            delay(1200)
            nextQuestion()
        }
    }

    private fun handleTimeExpired() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isAnswerCorrect = false,
                streak = 0
            )
        }

        viewModelScope.launch {
            delay(1200)
            nextQuestion()
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()
        val state = _uiState.value
        val avgReactionTimeMs = if (state.totalQuestions > 0) totalReactionTimeMs / state.totalQuestions else 0L

        _uiState.update {
            it.copy(
                isActive = false,
                isFinished = true,
                avgReactionTimeMs = avgReactionTimeMs
            )
        }

        viewModelScope.launch {
            repository.saveSession(
                modeName = state.quizType.title,
                score = state.score,
                totalQuestions = state.totalQuestions,
                avgReactionTimeMs = avgReactionTimeMs,
                maxStreak = state.maxStreak
            )
        }
    }

    fun resetQuiz() {
        timerJob?.cancel()
        _uiState.update {
            QuizUiState(quizType = it.quizType, difficulty = it.difficulty)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioEngine.release()
    }
}
