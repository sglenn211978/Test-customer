package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.InteractiveFretboard
import com.example.ui.components.SessionSummaryModal
import com.example.ui.viewmodel.QuizDifficulty
import com.example.ui.viewmodel.QuizType
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun NoteIdQuizScreen(
    viewModel: QuizViewModel,
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
            if (!state.isActive && !state.isFinished) {
                // Start Quiz Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_note_id_card")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Note Identification Challenge",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "A position on the fretboard will be highlighted with pitch audio. Identify the correct note name as fast as possible!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "DIFFICULTY LEVEL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuizDifficulty.entries.forEach { diff ->
                                val isSelected = (state.difficulty == diff)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDifficulty(diff) },
                                    label = { Text(diff.label) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.startQuiz(QuizType.NOTE_IDENTIFICATION) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_note_id_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Challenge", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (state.isFinished) {
                SessionSummaryModal(
                    modeTitle = state.quizType.title,
                    difficultyLabel = state.difficulty.label,
                    score = state.score,
                    correctCount = state.correctAnswersCount,
                    totalQuestions = state.totalQuestions,
                    avgReactionTimeMs = state.avgReactionTimeMs,
                    maxStreak = state.maxStreak,
                    onPlayAgain = { viewModel.startQuiz(QuizType.NOTE_IDENTIFICATION) },
                    onDismiss = { viewModel.resetQuiz() }
                )
            } else {
                // Active Quiz Layout
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Question ${state.questionNumber} / ${state.totalQuestions}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Score: ${state.score}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Streak: ${state.streak}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.replayEarTrainingAudio() }) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Replay Sound",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        val timerProgress = state.timeLeftSeconds.toFloat() / state.maxTimeSeconds.toFloat()
                        LinearProgressIndicator(
                            progress = { timerProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        InteractiveFretboard(
                            strings = state.strings,
                            totalFrets = state.difficulty.maxFret,
                            targetPosition = state.targetPosition,
                            onFretTapped = {},
                            modifier = Modifier.testTag("quiz_fretboard")
                        )
                    }
                }

                state.beginnerHint?.let { hint ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Hint: $hint",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    text = "What note is highlighted at String ${state.targetPosition?.stringNumber}, Fret ${state.targetPosition?.fretNumber}?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                state.isAnswerCorrect?.let { isCorrect ->
                    val responseSec = "%.2f".format(state.lastResponseTimeMs / 1000f)
                    val msg = if (isCorrect) "Correct! Speed: ${responseSec}s" else "Incorrect. Correct note: ${state.targetNote?.displayName}"
                    val color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }

                // 4 Choice Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.optionChoices.chunked(2).forEach { rowChoices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowChoices.forEach { optionNote ->
                                ElevatedIconButtonOption(
                                    displayName = optionNote.displayName,
                                    isEnabled = state.isAnswerCorrect == null,
                                    onClick = { viewModel.submitNoteIdAnswer(optionNote) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("choice_${optionNote.name}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElevatedIconButtonOption(
    displayName: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
