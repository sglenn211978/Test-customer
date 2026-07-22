package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FretPosition
import com.example.data.model.GuitarString
import com.example.ui.viewmodel.NoteHighlight
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FretWireColor
import com.example.ui.theme.GreenCorrect
import com.example.ui.theme.PearlInlayColor
import com.example.ui.theme.RedError
import com.example.ui.theme.StringBronzeColor
import com.example.ui.theme.StringSteelColor
import com.example.ui.theme.WoodFretboardDark

@Composable
fun InteractiveFretboard(
    strings: List<GuitarString>,
    totalFrets: Int = 12,
    highlights: Map<FretPosition, NoteHighlight> = emptyMap(),
    foundPositions: Set<FretPosition> = emptySet(),
    incorrectPositions: Set<FretPosition> = emptySet(),
    targetPosition: FretPosition? = null,
    onFretTapped: (FretPosition) -> Unit,
    modifier: Modifier = Modifier,
    fretWidthDp: Dp = 68.dp,
    stringHeightDp: Dp = 42.dp
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WoodFretboardDark)
            .border(1.5.dp, Color(0xFF3E3631), RoundedCornerShape(12.dp))
            .testTag("interactive_fretboard")
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            // Fret Numbering Bar at Top
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .padding(start = 50.dp) // Offset for open string column
            ) {
                for (fret in 0..totalFrets) {
                    Box(
                        modifier = Modifier
                            .width(fretWidthDp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (fret == 0) "Open" else "Fret $fret",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (fret == 12 || fret == 3 || fret == 5 || fret == 7 || fret == 9) AmberGoldPrimary else Color(0xFF9E9E9E),
                            fontWeight = if (fret == 12) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 6 Strings Rows (String 1 High E at top to String 6 Low E at bottom)
            strings.forEach { guitarString ->
                Row(
                    modifier = Modifier.height(stringHeightDp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // String Name Header Badge
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1B1816))
                            .border(0.5.dp, Color(0xFF332D29)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = guitarString.openNote.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = AmberGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Str ${guitarString.stringNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8D8D8D),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Frets for this string
                    for (fret in 0..totalFrets) {
                        val pos = FretPosition(guitarString.stringNumber, fret)
                        val highlight = highlights[pos]
                        val isFound = foundPositions.contains(pos)
                        val isIncorrect = incorrectPositions.contains(pos)
                        val isQuizTarget = (targetPosition == pos)

                        FretCell(
                            fretPosition = pos,
                            fretWidth = fretWidthDp,
                            stringGaugeMm = guitarString.gaugeMm,
                            stringNumber = guitarString.stringNumber,
                            isNut = (fret == 0),
                            hasInlayDot = (fret == 3 || fret == 5 || fret == 7 || fret == 9 || fret == 12),
                            isDoubleDot = (fret == 12),
                            highlight = highlight,
                            isFound = isFound,
                            isIncorrect = isIncorrect,
                            isQuizTarget = isQuizTarget,
                            onTap = { onFretTapped(pos) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FretCell(
    fretPosition: FretPosition,
    fretWidth: Dp,
    stringGaugeMm: Float,
    stringNumber: Int,
    isNut: Boolean,
    hasInlayDot: Boolean,
    isDoubleDot: Boolean,
    highlight: NoteHighlight?,
    isFound: Boolean,
    isIncorrect: Boolean,
    isQuizTarget: Boolean,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(fretWidth)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            )
            .testTag("fret_cell_${fretPosition.stringNumber}_${fretPosition.fretNumber}"),
        contentAlignment = Alignment.Center
    ) {
        // Background neck & fret lines canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Fret wire at right edge (or nut at fret 0)
            if (isNut) {
                drawRect(
                    color = Color(0xFFD7CCC8), // Bone nut
                    topLeft = Offset(width - 6.dp.toPx(), 0f),
                    size = androidx.compose.ui.geometry.Size(6.dp.toPx(), height)
                )
            } else {
                drawLine(
                    color = FretWireColor,
                    start = Offset(width, 0f),
                    end = Offset(width, height),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Inlay Dot Drawing (on String 3 line)
            if (hasInlayDot && stringNumber == 3) {
                if (isDoubleDot) {
                    drawCircle(
                        color = PearlInlayColor.copy(alpha = 0.45f),
                        radius = 4.dp.toPx(),
                        center = Offset(width / 2f - 7.dp.toPx(), centerY)
                    )
                    drawCircle(
                        color = PearlInlayColor.copy(alpha = 0.45f),
                        radius = 4.dp.toPx(),
                        center = Offset(width / 2f + 7.dp.toPx(), centerY)
                    )
                } else {
                    drawCircle(
                        color = PearlInlayColor.copy(alpha = 0.45f),
                        radius = 5.dp.toPx(),
                        center = Offset(width / 2f, centerY)
                    )
                }
            }

            // Guitar String Line (Thicker for lower bass strings)
            val strokeWidthPx = (stringGaugeMm * 3.2f).dp.toPx().coerceAtLeast(1.5f)
            val stringColor = if (stringNumber >= 4) StringBronzeColor else StringSteelColor

            drawLine(
                color = stringColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = strokeWidthPx
            )
        }

        // Active Note Badge Overlay
        val animatedScale by animateFloatAsState(
            targetValue = if (highlight != null || isFound || isIncorrect || isQuizTarget) 1.0f else 0f,
            label = "noteBadgeScale"
        )

        if (animatedScale > 0.05f) {
            val (badgeBgColor, badgeTextColor, borderStroke) = when {
                isFound -> Triple(GreenCorrect, Color.Black, BorderStrokeStyle(2.dp, Color.White))
                isIncorrect -> Triple(RedError, Color.White, BorderStrokeStyle(2.dp, Color.White))
                isQuizTarget -> Triple(CyanAccent, Color.Black, BorderStrokeStyle(2.dp, Color.White))
                highlight?.isRoot == true -> Triple(AmberGoldPrimary, Color.Black, BorderStrokeStyle(2.dp, Color.White))
                highlight?.isAccent == true -> Triple(CyanAccent, Color.Black, BorderStrokeStyle(1.dp, Color.White))
                else -> Triple(Color(0xFF37474F), Color.White, BorderStrokeStyle(1.dp, Color(0xFF78909C)))
            }

            val badgeText = when {
                isFound -> "✓"
                isIncorrect -> "✕"
                isQuizTarget -> "?"
                highlight != null -> highlight.labelText
                else -> ""
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(animatedScale)
                    .clip(CircleShape)
                    .background(badgeBgColor)
                    .border(borderStroke.width, borderStroke.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private data class BorderStrokeStyle(val width: Dp, val color: Color)
