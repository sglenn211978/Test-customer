package com.example.data.model

data class SongNoteStep(
    val stepTitle: String,
    val chordName: String? = null,
    val fretPositions: List<FretPosition>,
    val tabText: String,
    val durationMs: Long = 800L,
    val frequenciesHz: List<Float>
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val difficulty: String, // "Beginner (Frets 0-3)", "Easy", "Intermediate"
    val bpm: Int,
    val key: String,
    val description: String,
    val strummingPattern: String,
    val steps: List<SongNoteStep>
)

object SongLibrary {
    // Helper frequencies Hz for standard tuning
    // E2: 82.41, A2: 110.0, D3: 146.83, G3: 196.0, B3: 246.94, E4: 329.63
    private val E2 = 82.41f
    private val A2 = 110.0f
    private val D3 = 146.83f
    private val G3 = 196.0f
    private val B3 = 246.94f
    private val E4 = 329.63f

    // Helper formula for freq: base * 2^(fret/12)
    private fun freq(base: Float, fret: Int): Float = (base * Math.pow(2.0, fret / 12.0)).toFloat()

    val SONGS = listOf(
        Song(
            id = "seven_nation_army",
            title = "Seven Nation Army Riff",
            artist = "The White Stripes",
            difficulty = "Beginner (1 String)",
            bpm = 120,
            key = "E Minor",
            description = "The ultimate beginner guitar riff! Played entirely on String 5 (A string). Learn fret position spacing instantly.",
            strummingPattern = "Down Pluck Rhythm",
            steps = listOf(
                SongNoteStep("Opening E Note", null, listOf(FretPosition(5, 7)), "String 5, Fret 7 (E)", 600L, listOf(freq(A2, 7))),
                SongNoteStep("Repeat E Note", null, listOf(FretPosition(5, 7)), "String 5, Fret 7 (E)", 300L, listOf(freq(A2, 7))),
                SongNoteStep("Jump to G", null, listOf(FretPosition(5, 10)), "String 5, Fret 10 (G)", 400L, listOf(freq(A2, 10))),
                SongNoteStep("Back to E", null, listOf(FretPosition(5, 7)), "String 5, Fret 7 (E)", 400L, listOf(freq(A2, 7))),
                SongNoteStep("Down to D", null, listOf(FretPosition(5, 5)), "String 5, Fret 5 (D)", 400L, listOf(freq(A2, 5))),
                SongNoteStep("Down to C", null, listOf(FretPosition(5, 3)), "String 5, Fret 3 (C)", 600L, listOf(freq(A2, 3))),
                SongNoteStep("Finish on B", null, listOf(FretPosition(5, 2)), "String 5, Fret 2 (B)", 800L, listOf(freq(A2, 2)))
            )
        ),

        Song(
            id = "smoke_on_the_water",
            title = "Smoke on the Water",
            artist = "Deep Purple",
            difficulty = "Beginner (Power Riff)",
            bpm = 112,
            key = "G Minor",
            description = "The iconic double-stop riff on Strings 3 & 4 (D & G strings).",
            strummingPattern = "Thumb/Finger Pluck",
            steps = listOf(
                SongNoteStep("Open D & G", null, listOf(FretPosition(3, 0), FretPosition(4, 0)), "Strings 3 & 4 Open (G & D)", 500L, listOf(G3, D3)),
                SongNoteStep("Fret 3 Pluck", null, listOf(FretPosition(3, 3), FretPosition(4, 3)), "Strings 3 & 4, Fret 3", 500L, listOf(freq(G3, 3), freq(D3, 3))),
                SongNoteStep("Fret 5 Pluck", null, listOf(FretPosition(3, 5), FretPosition(4, 5)), "Strings 3 & 4, Fret 5", 700L, listOf(freq(G3, 5), freq(D3, 5))),
                SongNoteStep("Open D & G", null, listOf(FretPosition(3, 0), FretPosition(4, 0)), "Strings 3 & 4 Open", 500L, listOf(G3, D3)),
                SongNoteStep("Fret 3 Pluck", null, listOf(FretPosition(3, 3), FretPosition(4, 3)), "Strings 3 & 4, Fret 3", 500L, listOf(freq(G3, 3), freq(D3, 3))),
                SongNoteStep("Fret 6 Blue Note!", null, listOf(FretPosition(3, 6), FretPosition(4, 6)), "Strings 3 & 4, Fret 6", 300L, listOf(freq(G3, 6), freq(D3, 6))),
                SongNoteStep("Back to Fret 5", null, listOf(FretPosition(3, 5), FretPosition(4, 5)), "Strings 3 & 4, Fret 5", 800L, listOf(freq(G3, 5), freq(D3, 5)))
            )
        ),

        Song(
            id = "stand_by_me",
            title = "Stand By Me",
            artist = "Ben E. King",
            difficulty = "Beginner Chords",
            bpm = 118,
            key = "A Major",
            description = "Learn the 4 essential beginner chords: C Major -> A Minor -> F Major (Easy 3-string) -> G Major.",
            strummingPattern = "D - D - U - U - D - U",
            steps = listOf(
                SongNoteStep(
                    "C Major Chord",
                    "C Major",
                    listOf(FretPosition(5, 3), FretPosition(4, 2), FretPosition(2, 1)),
                    "Finger 3 on Str 5 Fret 3, Finger 2 on Str 4 Fret 2, Finger 1 on Str 2 Fret 1",
                    1200L,
                    listOf(freq(A2, 3), freq(D3, 2), G3, freq(B3, 1), E4)
                ),
                SongNoteStep(
                    "A Minor Chord",
                    "A Minor",
                    listOf(FretPosition(4, 2), FretPosition(3, 2), FretPosition(2, 1)),
                    "Finger 2 on Str 4 Fret 2, Finger 3 on Str 3 Fret 2, Finger 1 on Str 2 Fret 1",
                    1200L,
                    listOf(A2, freq(D3, 2), freq(G3, 2), freq(B3, 1), E4)
                ),
                SongNoteStep(
                    "Easy F Major Chord",
                    "F Major",
                    listOf(FretPosition(4, 3), FretPosition(3, 2), FretPosition(2, 1), FretPosition(1, 1)),
                    "Finger 3 on Str 4 Fret 3, Finger 2 on Str 3 Fret 2, Finger 1 flattening Str 1 & 2 Fret 1",
                    1200L,
                    listOf(freq(D3, 3), freq(G3, 2), freq(B3, 1), freq(E4, 1))
                ),
                SongNoteStep(
                    "G Major Chord",
                    "G Major",
                    listOf(FretPosition(6, 3), FretPosition(5, 2), FretPosition(1, 3)),
                    "Finger 2 on Str 6 Fret 3, Finger 1 on Str 5 Fret 2, Finger 3 on Str 1 Fret 3",
                    1200L,
                    listOf(freq(E2, 3), freq(A2, 2), D3, G3, B3, freq(E4, 3))
                )
            )
        ),

        Song(
            id = "knockin_on_heavens_door",
            title = "Knockin' On Heaven's Door",
            artist = "Bob Dylan",
            difficulty = "Beginner Open Chords",
            bpm = 70,
            key = "G Major",
            description = "Master the famous G -> D -> Am -> C open chord progression played by millions of guitarists.",
            strummingPattern = "D - D - D U - D U",
            steps = listOf(
                SongNoteStep(
                    "G Major Chord",
                    "G Major",
                    listOf(FretPosition(6, 3), FretPosition(5, 2), FretPosition(1, 3)),
                    "G Chord: Frets 3-2-0-0-0-3",
                    1400L,
                    listOf(freq(E2, 3), freq(A2, 2), D3, G3, B3, freq(E4, 3))
                ),
                SongNoteStep(
                    "D Major Chord",
                    "D Major",
                    listOf(FretPosition(3, 2), FretPosition(2, 3), FretPosition(1, 2)),
                    "D Chord: Str 3 Fret 2, Str 2 Fret 3, Str 1 Fret 2",
                    1400L,
                    listOf(D3, freq(G3, 2), freq(B3, 3), freq(E4, 2))
                ),
                SongNoteStep(
                    "A Minor Chord",
                    "A Minor",
                    listOf(FretPosition(4, 2), FretPosition(3, 2), FretPosition(2, 1)),
                    "Am Chord: Str 4 Fret 2, Str 3 Fret 2, Str 2 Fret 1",
                    1600L,
                    listOf(A2, freq(D3, 2), freq(G3, 2), freq(B3, 1), E4)
                ),
                SongNoteStep(
                    "C Major Chord",
                    "C Major",
                    listOf(FretPosition(5, 3), FretPosition(4, 2), FretPosition(2, 1)),
                    "C Chord: Str 5 Fret 3, Str 4 Fret 2, Str 2 Fret 1",
                    1600L,
                    listOf(freq(A2, 3), freq(D3, 2), G3, freq(B3, 1), E4)
                )
            )
        ),

        Song(
            id = "you_are_my_sunshine",
            title = "You Are My Sunshine Melody",
            artist = "Traditional",
            difficulty = "Beginner Single Notes",
            bpm = 90,
            key = "C Major",
            description = "Play the iconic vocal melody note-by-note on the bottom two strings (Frets 0 to 3).",
            strummingPattern = "Pluck Melodic Notes",
            steps = listOf(
                SongNoteStep("You", null, listOf(FretPosition(3, 0)), "G string Open", 400L, listOf(G3)),
                SongNoteStep("Are", null, listOf(FretPosition(3, 0)), "G string Open", 400L, listOf(G3)),
                SongNoteStep("My", null, listOf(FretPosition(3, 2)), "G string, Fret 2 (A)", 400L, listOf(freq(G3, 2))),
                SongNoteStep("Sun-", null, listOf(FretPosition(2, 0)), "B string Open", 500L, listOf(B3)),
                SongNoteStep("-shine", null, listOf(FretPosition(2, 0)), "B string Open", 600L, listOf(B3)),
                SongNoteStep("My", null, listOf(FretPosition(2, 0)), "B string Open", 400L, listOf(B3)),
                SongNoteStep("On-", null, listOf(FretPosition(3, 2)), "G string, Fret 2 (A)", 400L, listOf(freq(G3, 2))),
                SongNoteStep("-ly", null, listOf(FretPosition(2, 0)), "B string Open", 400L, listOf(B3)),
                SongNoteStep("Sun-", null, listOf(FretPosition(2, 1)), "B string, Fret 1 (C)", 600L, listOf(freq(B3, 1))),
                SongNoteStep("-shine", null, listOf(FretPosition(2, 1)), "B string, Fret 1 (C)", 800L, listOf(freq(B3, 1)))
            )
        )
    )
}
