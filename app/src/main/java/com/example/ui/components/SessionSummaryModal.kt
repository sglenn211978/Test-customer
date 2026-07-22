package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenCorrect
import com.example.ui.theme.RedError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class LevelInfo(
    val levelNumber: Int,
    val title: String,
    val currentXp: Int,
    val minXpForLevel: Int,
    val maxXpForLevel: Int
) {
    val progressFraction: Float
        get() {
            val range = maxXpForLevel - minXpForLevel
            if (range <= 0) return 1.0f
            return ((currentXp - minXpForLevel).toFloat() / range).coerceIn(0f, 1f)
        }
}

object LevelCalculator {
    fun calculateLevel(totalScoreXp: Int): LevelInfo {
        return when {
            totalScoreXp < 500 -> LevelInfo(1, "Novice Plucker", totalScoreXp, 0, 500)
            totalScoreXp < 1500 -> LevelInfo(2, "Fretboard Apprentice", totalScoreXp, 500, 1500)
            totalScoreXp < 3000 -> LevelInfo(3, "Rhythm Explorer", totalScoreXp, 1500, 3000)
            totalScoreXp < 6000 -> LevelInfo(4, "Neck Navigator", totalScoreXp, 3000, 6000)
            totalScoreXp < 10000 -> LevelInfo(5, "Scale Shredder", totalScoreXp, 6000, 10000)
            else -> LevelInfo(6, "Fretboard Master", totalScoreXp, 10000, 20000)
        }
    }
}

@Composable
fun SessionSummaryModal(
    modeTitle: String,
    difficultyLabel: String,
    score: Int,
    correctCount: Int,
    totalQuestions: Int,
    avgReactionTimeMs: Long,
    maxStreak: Int,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracyPct = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
    val accuracyColor = when {
        accuracyPct >= 80 -> GreenCorrect
        accuracyPct >= 50 -> AmberGoldPrimary
        else -> RedError
    }

    // Estimate total accumulated XP based on session score
    // Mock user cumulative score = score + 850 for demonstration of level progression
    val estimatedTotalXp = score + 850
    val levelInfo = LevelCalculator.calculateLevel(estimatedTotalXp)

    var animatedProgressTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedProgressTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "level_progress_anim"
    )

    LaunchedEffect(Unit) {
        animatedProgressTarget = levelInfo.progressFraction
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            tonalElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .border(2.dp, AmberGoldPrimary, RoundedCornerShape(20.dp))
                .testTag("session_summary_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Trophy Icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AmberGoldPrimary.copy(alpha = 0.2f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Session Complete",
                        tint = AmberGoldPrimary,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "TRAINING COMPLETE",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = modeTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = difficultyLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Metrics Row: Accuracy & Total Notes Hit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Accuracy Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_accuracy_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ACCURACY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$accuracyPct%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = accuracyColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Total Notes Hit Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_notes_hit_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NOTES HIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$correctCount / $totalQuestions",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Row: Score, Streak, Reaction Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGoldPrimary, modifier = Modifier.padding(end = 4.dp))
                        Text(text = "$score pts", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = RedError, modifier = Modifier.padding(end = 4.dp))
                        Text(text = "$maxStreak streak", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (avgReactionTimeMs > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.padding(end = 4.dp))
                            Text(text = "%.2fs".format(avgReactionTimeMs / 1000f), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skill Level Progress Container
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_skill_level_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LEVEL ${levelInfo.levelNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = levelInfo.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AmberGoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+$score XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AmberGoldPrimary,
                            trackColor = DarkSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${levelInfo.currentXp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "Next: ${levelInfo.maxXpForLevel} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_dismiss_btn")
                    ) {
                        Text("Done", color = TextPrimary)
                    }

                    Button(
                        onClick = onPlayAgain,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGoldPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("summary_play_again_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
