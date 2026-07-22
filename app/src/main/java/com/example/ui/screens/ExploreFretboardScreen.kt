package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GuitarString
import com.example.data.model.IntervalType
import com.example.data.model.NoteName
import com.example.data.model.ScaleType
import com.example.ui.components.InteractiveFretboard
import com.example.ui.viewmodel.ExplorationMode
import com.example.ui.viewmodel.FretRange
import com.example.ui.viewmodel.FretboardDisplayMode
import com.example.ui.viewmodel.FretboardViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreFretboardScreen(
    viewModel: FretboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fretboard Explorer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Interactive neck visualizer, scales & pitch analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { viewModel.toggleBeginnerGuide() }) {
                    Icon(
                        imageVector = if (state.showBeginnerGuide) Icons.Default.ExpandLess else Icons.Default.School,
                        contentDescription = "Toggle Guide",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Beginner Guide Accordion
            AnimatedVisibility(
                visible = state.showBeginnerGuide,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("beginner_guide_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Beginner Quick Guide",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.applyBeginnerPreset() }
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Reset View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Strings 1–6 run top-to-bottom: String 1 is High E (top), String 6 is Low E (bottom).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fret 0 represents open strings without pressing any frets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tip: Start with Frets 0–3 scope to learn basic open position notes (C, D, E, F, G, A, B).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Exploration Modes Horizontal Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExplorationMode.entries.forEach { mode ->
                    val isSelected = state.explorationMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setExplorationMode(mode) },
                        label = { Text(mode.label) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.MusicNote, contentDescription = null) }
                        } else null,
                        modifier = Modifier.testTag("mode_tab_${mode.name}")
                    )
                }
            }

            // Scope Range Filter Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "FRETBOARD SCOPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FretRange.entries.forEach { range ->
                        val isSelected = state.fretRange == range
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFretRange(range) },
                            label = { Text(range.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Interactive Fretboard Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    InteractiveFretboard(
                        strings = state.strings,
                        totalFrets = state.totalFrets,
                        highlights = state.highlights,
                        onFretTapped = { pos -> viewModel.onFretPositionTapped(pos) },
                        modifier = Modifier.testTag("interactive_fretboard")
                    )
                }
            }

            // Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { viewModel.playCurrentScaleArpeggio() },
                    modifier = Modifier.testTag("play_arpeggio_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arpeggio Play", fontWeight = FontWeight.SemiBold)
                }

                // Display Mode Segmented Control
                SingleChoiceSegmentedButtonRow {
                    FretboardDisplayMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.displayMode == mode,
                            onClick = { viewModel.setDisplayMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = FretboardDisplayMode.entries.size
                            )
                        ) {
                            Text(
                                text = when (mode) {
                                    FretboardDisplayMode.NOTE_NAMES -> "Notes"
                                    FretboardDisplayMode.SCALE_DEGREES -> "Degrees"
                                    FretboardDisplayMode.OCTAVES -> "Octaves"
                                    FretboardDisplayMode.HIDDEN -> "Blank"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Configuration Panels based on Mode
            when (state.explorationMode) {
                ExplorationMode.SCALES -> {
                    ScaleConfigurationCard(
                        selectedRoot = state.selectedScaleRoot,
                        selectedScaleType = state.selectedScaleType,
                        onRootSelected = { viewModel.setScaleRoot(it) },
                        onScaleTypeSelected = { viewModel.setScaleType(it) }
                    )
                }
                ExplorationMode.INTERVALS -> {
                    IntervalConfigurationCard(
                        selectedRoot = state.selectedIntervalRoot,
                        selectedInterval = state.selectedIntervalType,
                        onRootSelected = { viewModel.setIntervalRoot(it) },
                        onIntervalSelected = { viewModel.setIntervalType(it) }
                    )
                }
                ExplorationMode.PITCH_PIPE -> {
                    PitchPipeCard(
                        strings = state.strings,
                        activeString = state.activeTunerString,
                        isPlaying = state.isPitchPipePlaying,
                        onTogglePipe = { viewModel.togglePitchPipe(it) },
                        onStop = { viewModel.stopPitchPipe() }
                    )
                }
                ExplorationMode.FREE_PLAY -> {
                    FreePlayCard(
                        lastTapped = state.lastTappedPosition,
                        strings = state.strings
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScaleConfigurationCard(
    selectedRoot: NoteName,
    selectedScaleType: ScaleType,
    onRootSelected: (NoteName) -> Unit,
    onScaleTypeSelected: (ScaleType) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scale_config_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Root Key",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteName.ALL_NOTES.forEach { note ->
                    val isSelected = (note == selectedRoot)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRootSelected(note) },
                        label = { Text(note.displayName, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("root_note_${note.name}")
                    )
                }
            }

            Text(
                text = "Scale Formula",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScaleType.entries.forEach { scale ->
                    val isSelected = (scale == selectedScaleType)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onScaleTypeSelected(scale) },
                        label = { Text(scale.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.testTag("scale_type_${scale.name}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntervalConfigurationCard(
    selectedRoot: NoteName,
    selectedInterval: IntervalType,
    onRootSelected: (NoteName) -> Unit,
    onIntervalSelected: (IntervalType) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interval_config_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Reference Root Note",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteName.ALL_NOTES.forEach { note ->
                    val isSelected = (note == selectedRoot)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRootSelected(note) },
                        label = { Text(note.displayName, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Text(
                text = "Target Interval",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntervalType.entries.forEach { interval ->
                    val isSelected = (interval == selectedInterval)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onIntervalSelected(interval) },
                        label = { Text("${interval.shortCode} - ${interval.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PitchPipeCard(
    strings: List<GuitarString>,
    activeString: GuitarString?,
    isPlaying: Boolean,
    onTogglePipe: (GuitarString) -> Unit,
    onStop: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pitch_pipe_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pitch Pipe Tuner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isPlaying) {
                    IconButton(onClick = onStop) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                strings.forEach { str ->
                    val isThisActive = (isPlaying && activeString == str)
                    FilterChip(
                        selected = isThisActive,
                        onClick = { onTogglePipe(str) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(str.openNote.displayName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${str.baseFrequencyHz.toInt()}Hz",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pitch_pipe_string_${str.stringNumber}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FreePlayCard(
    lastTapped: com.example.data.model.FretPosition?,
    strings: List<GuitarString>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Free Neck Exploration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (lastTapped != null) {
                val note = lastTapped.getNoteName(strings)
                val freq = lastTapped.getFrequencyHz(strings)
                Text(
                    text = "Last Tapped: String ${lastTapped.stringNumber}, Fret ${lastTapped.fretNumber} → ${note.displayName} (${freq.toInt()} Hz)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "Tap any string or fret above to play audio and inspect note positions across all 12 frets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
