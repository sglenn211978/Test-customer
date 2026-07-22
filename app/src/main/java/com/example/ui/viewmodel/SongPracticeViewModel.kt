package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GuitarAudioEngine
import com.example.data.model.FretPosition
import com.example.data.model.GuitarString
import com.example.data.model.Song
import com.example.data.model.SongLibrary
import com.example.data.model.StandardTuning
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SongPracticeUiState(
    val songs: List<Song> = SongLibrary.SONGS,
    val selectedSong: Song = SongLibrary.SONGS[0],
    val currentStepIndex: Int = 0,
    val isPlaying: Boolean = false,
    val tempoMultiplier: Float = 1.0f, // 0.5f to 1.5f speed
    val strings: List<GuitarString> = StandardTuning.STRINGS,
    
    // Practice Quiz Mode
    val isInteractivePracticeMode: Boolean = false,
    val userTappedPositions: Set<FretPosition> = emptySet(),
    val isStepMastered: Boolean = false,
    val feedbackMessage: String? = null,
    val masteredStepIndices: Set<Int> = emptySet(),
    val showSummaryModal: Boolean = false
)

class SongPracticeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SongPracticeUiState())
    val uiState: StateFlow<SongPracticeUiState> = _uiState.asStateFlow()

    private val audioEngine = GuitarAudioEngine()
    private var playbackJob: Job? = null

    fun selectSong(song: Song) {
        stopSong()
        _uiState.update {
            it.copy(
                selectedSong = song,
                currentStepIndex = 0,
                isPlaying = false,
                userTappedPositions = emptySet(),
                isStepMastered = false,
                feedbackMessage = null,
                masteredStepIndices = emptySet(),
                showSummaryModal = false
            )
        }
    }

    fun setTempoMultiplier(speed: Float) {
        _uiState.update { it.copy(tempoMultiplier = speed) }
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            stopSong()
        } else {
            startSongPlayback()
        }
    }

    fun startSongPlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }

        playbackJob = viewModelScope.launch {
            val song = _uiState.value.selectedSong
            var index = _uiState.value.currentStepIndex

            while (_uiState.value.isPlaying && index < song.steps.size) {
                _uiState.update { it.copy(currentStepIndex = index) }
                val step = song.steps[index]

                // Play step audio through audio engine
                step.frequenciesHz.forEach { freq ->
                    audioEngine.playPluckedNote(freq, 1.5f)
                }

                val durationWithSpeed = (step.durationMs / _uiState.value.tempoMultiplier).toLong()
                delay(durationWithSpeed.coerceAtLeast(200L))

                index++
                if (index >= song.steps.size) {
                    index = 0 // Loop or finish
                }
            }
        }
    }

    fun stopSong() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun goToNextStep() {
        stopSong()
        val song = _uiState.value.selectedSong
        val nextIdx = (_uiState.value.currentStepIndex + 1) % song.steps.size
        _uiState.update {
            it.copy(
                currentStepIndex = nextIdx,
                userTappedPositions = emptySet(),
                isStepMastered = false,
                feedbackMessage = null
            )
        }
        playCurrentStepAudio()
    }

    fun goToPreviousStep() {
        stopSong()
        val song = _uiState.value.selectedSong
        val prevIdx = if (_uiState.value.currentStepIndex - 1 < 0) song.steps.size - 1 else _uiState.value.currentStepIndex - 1
        _uiState.update {
            it.copy(
                currentStepIndex = prevIdx,
                userTappedPositions = emptySet(),
                isStepMastered = false,
                feedbackMessage = null
            )
        }
        playCurrentStepAudio()
    }

    fun selectStep(index: Int) {
        stopSong()
        val song = _uiState.value.selectedSong
        if (index in song.steps.indices) {
            _uiState.update {
                it.copy(
                    currentStepIndex = index,
                    userTappedPositions = emptySet(),
                    isStepMastered = false,
                    feedbackMessage = null
                )
            }
            playCurrentStepAudio()
        }
    }

    fun playCurrentStepAudio() {
        val song = _uiState.value.selectedSong
        val step = song.steps[_uiState.value.currentStepIndex]
        step.frequenciesHz.forEach { freq ->
            audioEngine.playPluckedNote(freq, 1.5f)
        }
    }

    fun togglePracticeMode(enabled: Boolean) {
        stopSong()
        _uiState.update {
            it.copy(
                isInteractivePracticeMode = enabled,
                userTappedPositions = emptySet(),
                isStepMastered = false,
                feedbackMessage = null
            )
        }
    }

    fun handleFretTapInPractice(position: FretPosition) {
        val song = _uiState.value.selectedSong
        val currentIdx = _uiState.value.currentStepIndex
        val currentStep = song.steps[currentIdx]
        val requiredPositions = currentStep.fretPositions.toSet()

        val newTapped = _uiState.value.userTappedPositions.toMutableSet()
        if (newTapped.contains(position)) {
            newTapped.remove(position)
        } else {
            newTapped.add(position)
        }

        val isMastered = newTapped == requiredPositions
        val newMasteredIndices = _uiState.value.masteredStepIndices.toMutableSet()
        if (isMastered) {
            newMasteredIndices.add(currentIdx)
            playCurrentStepAudio()
        }

        val isSongComplete = newMasteredIndices.size == song.steps.size
        val feedback = when {
            isSongComplete -> "🎉 Outstanding! You completed all steps in this song!"
            isMastered -> "✨ Great job! Step ${currentIdx + 1} mastered!"
            else -> null
        }

        _uiState.update {
            it.copy(
                userTappedPositions = newTapped,
                isStepMastered = isMastered,
                masteredStepIndices = newMasteredIndices,
                showSummaryModal = isSongComplete,
                feedbackMessage = feedback
            )
        }
    }

    fun dismissSummaryModal() {
        _uiState.update {
            it.copy(
                showSummaryModal = false,
                masteredStepIndices = emptySet(),
                currentStepIndex = 0,
                userTappedPositions = emptySet(),
                feedbackMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
