package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
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
import com.example.ui.components.InteractiveFretboard
import com.example.ui.components.SessionSummaryModal
import com.example.ui.viewmodel.SongPracticeViewModel

@Composable
fun SongPracticeScreen(
    viewModel: SongPracticeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val currentSong = state.selectedSong
    val currentStep = currentSong.steps.getOrNull(state.currentStepIndex) ?: currentSong.steps[0]

    if (state.showSummaryModal) {
        SessionSummaryModal(
            modeTitle = currentSong.title,
            difficultyLabel = currentSong.difficulty,
            score = 300,
            correctCount = currentSong.steps.size,
            totalQuestions = currentSong.steps.size,
            avgReactionTimeMs = 0L,
            maxStreak = currentSong.steps.size,
            onPlayAgain = { viewModel.dismissSummaryModal() },
            onDismiss = { viewModel.dismissSummaryModal() }
        )
    }

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
            Column {
                Text(
                    text = "Song Practice Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Apply fretboard knowledge to real song riffs & chords",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Song Selection Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("song_carousel")
            ) {
                itemsIndexed(state.songs) { _, song ->
                    val isSelected = (song.id == currentSong.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectSong(song) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${song.artist} • ${song.difficulty}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    )
                }
            }

            // Selected Song Details
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_song_card")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "By ${currentSong.artist} • Key of ${currentSong.key}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AssistChip(
                            onClick = { },
                            label = { Text("${currentSong.bpm} BPM") },
                            leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) }
                        )
                    }

                    Text(
                        text = currentSong.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Strumming:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentSong.strummingPattern,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Fretboard Visualizer Section Title
            Text(
                text = "STEP ${state.currentStepIndex + 1} OF ${currentSong.steps.size}: ${currentStep.stepTitle.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val highlightedPositions = if (state.isInteractivePracticeMode) state.userTappedPositions else currentStep.fretPositions.toSet()

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    InteractiveFretboard(
                        strings = state.strings,
                        totalFrets = 12,
                        foundPositions = highlightedPositions,
                        onFretTapped = { pos ->
                            if (state.isInteractivePracticeMode) {
                                viewModel.handleFretTapInPractice(pos)
                            }
                        },
                        modifier = Modifier.testTag("song_fretboard_visualizer")
                    )
                }
            }

            // Step Detail Box & Audio Trigger
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        currentStep.chordName?.let { chord ->
                            Text(
                                text = chord,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = currentStep.tabText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { viewModel.playCurrentStepAudio() }) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Play Sound",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Practice Feedback Message
            state.feedbackMessage?.let { feedback ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Controls Panel Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("song_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.goToPreviousStep() },
                            modifier = Modifier.testTag("song_prev_step")
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Previous")
                        }

                        Button(
                            onClick = { viewModel.togglePlayback() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("song_play_pause_button")
                        ) {
                            Icon(
                                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (state.isPlaying) "Pause" else "Play Song",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.goToNextStep() },
                            modifier = Modifier.testTag("song_next_step")
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Next")
                        }
                    }

                    // Interactive Practice Mode Toggle
                    FilledTonalButton(
                        onClick = { viewModel.togglePracticeMode(!state.isInteractivePracticeMode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_practice_mode")
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (state.isInteractivePracticeMode) "Practice Mode Active (Tap Fretboard)" else "Enable Interactive Practice Mode",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tempo Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speed: ${(state.tempoMultiplier * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(100.dp)
                        )
                        Slider(
                            value = state.tempoMultiplier,
                            onValueChange = { viewModel.setTempoMultiplier(it) },
                            valueRange = 0.5f..1.5f,
                            steps = 3,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tempo_slider")
                        )
                    }
                }
            }

            // Timeline Steps Filter Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TIMELINE STEPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentSong.steps.forEachIndexed { idx, step ->
                        val isCurrent = (idx == state.currentStepIndex)
                        FilterChip(
                            selected = isCurrent,
                            onClick = { viewModel.selectStep(idx) },
                            label = { Text("${idx + 1}. ${step.stepTitle}") }
                        )
                    }
                }
            }
        }
    }
}
