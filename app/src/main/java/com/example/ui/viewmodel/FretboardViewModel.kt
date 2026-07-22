package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GuitarAudioEngine
import com.example.data.model.FretPosition
import com.example.data.model.GuitarString
import com.example.data.model.IntervalType
import com.example.data.model.NoteName
import com.example.data.model.ScaleType
import com.example.data.model.StandardTuning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FretboardDisplayMode(val label: String) {
    NOTE_NAMES("Note Names"),
    SCALE_DEGREES("Scale Degrees"),
    OCTAVES("Octaves / Frequencies"),
    HIDDEN("Blank / Quiz Mode")
}

enum class ExplorationMode(val label: String) {
    SCALES("Scales & Modes"),
    INTERVALS("Interval Mapping"),
    PITCH_PIPE("Pitch Pipe Tuner"),
    FREE_PLAY("Free Exploration")
}

data class NoteHighlight(
    val position: FretPosition,
    val noteName: NoteName,
    val labelText: String,
    val isRoot: Boolean = false,
    val isAccent: Boolean = false,
    val customColorHex: Long? = null
)

enum class FretRange(val label: String, val maxFret: Int) {
    OPEN_POSITION("Frets 0–3 (Beginner)", 3),
    FIRST_POSITION("Frets 0–5 (Essential)", 5),
    FULL_NECK("Frets 0–12 (Full Neck)", 12)
}

data class FretboardUiState(
    val totalFrets: Int = 3, // Default to beginner 3 frets
    val fretRange: FretRange = FretRange.OPEN_POSITION,
    val naturalNotesOnly: Boolean = true, // Default to beginner natural notes
    val showBeginnerGuide: Boolean = true, // Beginner guide banner open by default
    val strings: List<GuitarString> = StandardTuning.STRINGS,
    val explorationMode: ExplorationMode = ExplorationMode.SCALES,
    val displayMode: FretboardDisplayMode = FretboardDisplayMode.NOTE_NAMES,
    
    // Scale settings
    val selectedScaleRoot: NoteName = NoteName.C,
    val selectedScaleType: ScaleType = ScaleType.PENTATONIC_MAJOR,
    
    // Interval settings
    val selectedIntervalRoot: NoteName = NoteName.C,
    val selectedIntervalType: IntervalType = IntervalType.PERFECT_5TH,

    // Pitch Pipe / Tuner
    val activeTunerString: GuitarString? = null,
    val isPitchPipePlaying: Boolean = false,

    // Computed highlights on fretboard
    val highlights: Map<FretPosition, NoteHighlight> = emptyMap(),
    
    // Last user interaction
    val lastTappedPosition: FretPosition? = null
)

class FretboardViewModel : ViewModel() {

    private val audioEngine = GuitarAudioEngine()

    private val _uiState = MutableStateFlow(FretboardUiState())
    val uiState: StateFlow<FretboardUiState> = _uiState.asStateFlow()

    init {
        updateHighlights()
    }

    fun setFretRange(range: FretRange) {
        _uiState.update { it.copy(fretRange = range, totalFrets = range.maxFret) }
        updateHighlights()
    }

    fun toggleNaturalNotesOnly(enabled: Boolean) {
        _uiState.update { it.copy(naturalNotesOnly = enabled) }
        updateHighlights()
    }

    fun toggleBeginnerGuide() {
        _uiState.update { it.copy(showBeginnerGuide = !it.showBeginnerGuide) }
    }

    fun applyBeginnerPreset() {
        _uiState.update {
            it.copy(
                fretRange = FretRange.OPEN_POSITION,
                totalFrets = 3,
                naturalNotesOnly = true,
                explorationMode = ExplorationMode.SCALES,
                selectedScaleRoot = NoteName.C,
                selectedScaleType = ScaleType.MAJOR,
                displayMode = FretboardDisplayMode.NOTE_NAMES
            )
        }
        updateHighlights()
    }

    fun setExplorationMode(mode: ExplorationMode) {
        _uiState.update { it.copy(explorationMode = mode) }
        updateHighlights()
    }

    fun setDisplayMode(mode: FretboardDisplayMode) {
        _uiState.update { it.copy(displayMode = mode) }
        updateHighlights()
    }

    fun setScaleRoot(root: NoteName) {
        _uiState.update { it.copy(selectedScaleRoot = root) }
        updateHighlights()
    }

    fun setScaleType(scaleType: ScaleType) {
        _uiState.update { it.copy(selectedScaleType = scaleType) }
        updateHighlights()
    }

    fun setIntervalRoot(root: NoteName) {
        _uiState.update { it.copy(selectedIntervalRoot = root) }
        updateHighlights()
    }

    fun setIntervalType(intervalType: IntervalType) {
        _uiState.update { it.copy(selectedIntervalType = intervalType) }
        updateHighlights()
    }

    fun onFretPositionTapped(pos: FretPosition) {
        val freq = pos.getFrequencyHz(_uiState.value.strings)
        audioEngine.playPluckedNote(freq)
        _uiState.update { it.copy(lastTappedPosition = pos) }
    }

    fun playCurrentScaleArpeggio() {
        val currentHighlights = _uiState.value.highlights.values.toList()
            .sortedBy { it.position.fretNumber * 10 + it.position.stringNumber }
        
        val freqs = currentHighlights.map { it.position.getFrequencyHz(_uiState.value.strings) }.distinct().take(12)
        audioEngine.playArpeggio(freqs)
    }

    fun togglePitchPipe(string: GuitarString) {
        val currentState = _uiState.value
        if (currentState.isPitchPipePlaying && currentState.activeTunerString == string) {
            audioEngine.stopPitchPipe()
            _uiState.update { it.copy(isPitchPipePlaying = false, activeTunerString = null) }
        } else {
            audioEngine.startPitchPipe(string.baseFrequencyHz)
            _uiState.update { it.copy(isPitchPipePlaying = true, activeTunerString = string) }
        }
    }

    fun stopPitchPipe() {
        audioEngine.stopPitchPipe()
        _uiState.update { it.copy(isPitchPipePlaying = false, activeTunerString = null) }
    }

    private fun updateHighlights() {
        val state = _uiState.value
        val map = mutableMapOf<FretPosition, NoteHighlight>()

        when (state.explorationMode) {
            ExplorationMode.SCALES -> {
                val scaleNotes = state.selectedScaleType.getNotesInScale(state.selectedScaleRoot)
                val rootNote = state.selectedScaleRoot

                for (str in state.strings) {
                    for (fret in 0..state.totalFrets) {
                        val pos = FretPosition(str.stringNumber, fret)
                        val noteName = pos.getNoteName(state.strings)

                        if (scaleNotes.contains(noteName)) {
                            val isRoot = (noteName == rootNote)
                            val degreeLabel = state.selectedScaleType.getDegreeName(rootNote, noteName) ?: noteName.displayName
                            val label = when (state.displayMode) {
                                FretboardDisplayMode.NOTE_NAMES -> noteName.displayName
                                FretboardDisplayMode.SCALE_DEGREES -> degreeLabel
                                FretboardDisplayMode.OCTAVES -> "${noteName.displayName}${pos.getOctave(state.strings)}"
                                FretboardDisplayMode.HIDDEN -> ""
                            }

                            map[pos] = NoteHighlight(
                                position = pos,
                                noteName = noteName,
                                labelText = label,
                                isRoot = isRoot,
                                isAccent = !isRoot
                            )
                        }
                    }
                }
            }
            ExplorationMode.INTERVALS -> {
                val root = state.selectedIntervalRoot
                val targetNote = NoteName.fromSemitone(root.semitoneFromC + state.selectedIntervalType.semitones)

                for (str in state.strings) {
                    for (fret in 0..state.totalFrets) {
                        val pos = FretPosition(str.stringNumber, fret)
                        val noteName = pos.getNoteName(state.strings)

                        if (noteName == root) {
                            map[pos] = NoteHighlight(
                                position = pos,
                                noteName = noteName,
                                labelText = if (state.displayMode == FretboardDisplayMode.NOTE_NAMES) "Root ($root)" else "1",
                                isRoot = true
                            )
                        } else if (noteName == targetNote) {
                            map[pos] = NoteHighlight(
                                position = pos,
                                noteName = noteName,
                                labelText = if (state.displayMode == FretboardDisplayMode.NOTE_NAMES) "$noteName (${state.selectedIntervalType.shortCode})" else state.selectedIntervalType.shortCode,
                                isRoot = false,
                                isAccent = true
                            )
                        }
                    }
                }
            }
            ExplorationMode.PITCH_PIPE -> {
                // Highlight open strings
                for (str in state.strings) {
                    val pos = FretPosition(str.stringNumber, 0)
                    map[pos] = NoteHighlight(
                        position = pos,
                        noteName = str.openNote,
                        labelText = "${str.openNote.displayName}${str.openOctave}",
                        isRoot = (state.activeTunerString == str)
                    )
                }
            }
            ExplorationMode.FREE_PLAY -> {
                // Highlight all notes when clicked or all
                for (str in state.strings) {
                    for (fret in 0..state.totalFrets) {
                        val pos = FretPosition(str.stringNumber, fret)
                        val noteName = pos.getNoteName(state.strings)
                        map[pos] = NoteHighlight(
                            position = pos,
                            noteName = noteName,
                            labelText = when (state.displayMode) {
                                FretboardDisplayMode.NOTE_NAMES -> noteName.displayName
                                FretboardDisplayMode.SCALE_DEGREES -> noteName.displayName
                                FretboardDisplayMode.OCTAVES -> "${noteName.displayName}${pos.getOctave(state.strings)}"
                                FretboardDisplayMode.HIDDEN -> "?"
                            },
                            isRoot = false
                        )
                    }
                }
            }
        }

        _uiState.update { it.copy(highlights = map) }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
