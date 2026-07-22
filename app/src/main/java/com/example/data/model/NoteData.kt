package com.example.data.model

import java.util.Locale

/**
 * Representing the 12 chromatic notes in music theory.
 */
enum class NoteName(val displayName: String, val altName: String? = null, val semitoneFromC: Int) {
    C("C", null, 0),
    C_SHARP("C#", "Db", 1),
    D("D", null, 2),
    D_SHARP("D#", "Eb", 3),
    E("E", null, 4),
    F("F", null, 5),
    F_SHARP("F#", "Gb", 6),
    G("G", null, 7),
    G_SHARP("G#", "Ab", 8),
    A("A", null, 9),
    A_SHARP("A#", "Bb", 10),
    B("B", null, 11);

    companion object {
        val ALL_NOTES = entries.toTypedArray()

        fun fromSemitone(semitone: Int): NoteName {
            val normalized = (semitone % 12 + 12) % 12
            return ALL_NOTES.first { it.semitoneFromC == normalized }
        }
    }
}

/**
 * Standard 6-string guitar tuning (12 frets standard).
 * String 1: High E (E4)
 * String 2: B (B3)
 * String 3: G (G3)
 * String 4: D (D3)
 * String 5: A (A2)
 * String 6: Low E (E2)
 */
data class GuitarString(
    val stringNumber: Int, // 1 (High E) to 6 (Low E)
    val openNote: NoteName,
    val openOctave: Int,
    val baseFrequencyHz: Float,
    val gaugeMm: Float // String gauge representation for UI
)

object StandardTuning {
    val STRINGS = listOf(
        GuitarString(1, NoteName.E, 4, 329.63f, 0.25f), // High E
        GuitarString(2, NoteName.B, 3, 246.94f, 0.33f), // B
        GuitarString(3, NoteName.G, 3, 196.00f, 0.43f), // G
        GuitarString(4, NoteName.D, 3, 146.83f, 0.66f), // D
        GuitarString(5, NoteName.A, 2, 110.00f, 0.91f), // A
        GuitarString(6, NoteName.E, 2, 82.41f, 1.17f)   // Low E
    )
}

/**
 * Location on guitar neck
 */
data class FretPosition(
    val stringNumber: Int, // 1 to 6
    val fretNumber: Int     // 0 (Open) to 12
) {
    fun getNoteName(strings: List<GuitarString> = StandardTuning.STRINGS): NoteName {
        val str = strings.firstOrNull { it.stringNumber == stringNumber } ?: StandardTuning.STRINGS[0]
        return NoteName.fromSemitone(str.openNote.semitoneFromC + fretNumber)
    }

    fun getOctave(strings: List<GuitarString> = StandardTuning.STRINGS): Int {
        val str = strings.firstOrNull { it.stringNumber == stringNumber } ?: StandardTuning.STRINGS[0]
        val totalSemitones = str.openNote.semitoneFromC + fretNumber
        val octaveShift = totalSemitones / 12
        return str.openOctave + octaveShift
    }

    fun getFrequencyHz(strings: List<GuitarString> = StandardTuning.STRINGS): Float {
        val str = strings.firstOrNull { it.stringNumber == stringNumber } ?: StandardTuning.STRINGS[0]
        return (str.baseFrequencyHz * Math.pow(2.0, fretNumber / 12.0)).toFloat()
    }
}

/**
 * Musical Scale Definitions
 */
enum class ScaleType(
    val displayName: String,
    val intervalsFromRoot: List<Int>, // Semitones from root
    val intervalNames: List<String>
) {
    CHROMATIC("Chromatic", (0..11).toList(), listOf("1", "m2", "2", "m3", "3", "4", "b5", "5", "m6", "6", "m7", "7")),
    MAJOR("Major (Ionian)", listOf(0, 2, 4, 5, 7, 9, 11), listOf("1", "2", "3", "4", "5", "6", "7")),
    NATURAL_MINOR("Natural Minor (Aeolian)", listOf(0, 2, 3, 5, 7, 8, 10), listOf("1", "2", "b3", "4", "5", "b6", "b7")),
    PENTATONIC_MAJOR("Major Pentatonic", listOf(0, 2, 4, 7, 9), listOf("1", "2", "3", "5", "6")),
    PENTATONIC_MINOR("Minor Pentatonic", listOf(0, 3, 5, 7, 10), listOf("1", "b3", "4", "5", "b7")),
    BLUES("Blues Scale", listOf(0, 3, 5, 6, 7, 10), listOf("1", "b3", "4", "b5", "5", "b7")),
    DORIAN("Dorian Mode", listOf(0, 2, 3, 5, 7, 9, 10), listOf("1", "2", "b3", "4", "5", "6", "b7")),
    MIXOLYDIAN("Mixolydian Mode", listOf(0, 2, 4, 5, 7, 9, 10), listOf("1", "2", "3", "4", "5", "6", "b7")),
    HARMONIC_MINOR("Harmonic Minor", listOf(0, 2, 3, 5, 7, 8, 11), listOf("1", "2", "b3", "4", "5", "b6", "7")),
    MAJOR_TRIAD("Major Triad", listOf(0, 4, 7), listOf("1", "3", "5")),
    MINOR_TRIAD("Minor Triad", listOf(0, 3, 7), listOf("1", "b3", "5")),
    DOMINANT_7TH("Dominant 7th Arpeggio", listOf(0, 4, 7, 10), listOf("1", "3", "5", "b7"));

    fun getNotesInScale(root: NoteName): List<NoteName> {
        return intervalsFromRoot.map { NoteName.fromSemitone(root.semitoneFromC + it) }
    }

    fun getDegreeName(root: NoteName, targetNote: NoteName): String? {
        val semitones = (targetNote.semitoneFromC - root.semitoneFromC + 12) % 12
        val index = intervalsFromRoot.indexOf(semitones)
        return if (index >= 0) intervalNames[index] else null
    }
}

/**
 * Musical Interval definitions
 */
enum class IntervalType(
    val displayName: String,
    val shortCode: String,
    val semitones: Int
) {
    UNISON("Perfect Unison", "P1", 0),
    MINOR_2ND("Minor 2nd", "m2", 1),
    MAJOR_2ND("Major 2nd", "M2", 2),
    MINOR_3RD("Minor 3rd", "m3", 3),
    MAJOR_3RD("Major 3rd", "M3", 4),
    PERFECT_4TH("Perfect 4th", "P4", 5),
    TRITONE("Tritone / Dim 5th", "d5", 6),
    PERFECT_5TH("Perfect 5th", "P5", 7),
    MINOR_6TH("Minor 6th", "m6", 8),
    MAJOR_6TH("Major 6th", "M6", 9),
    MINOR_7TH("Minor 7th", "m7", 10),
    MAJOR_7TH("Major 7th", "M7", 11),
    OCTAVE("Octave", "P8", 12);

    companion object {
        fun fromSemitones(semitones: Int): IntervalType {
            val norm = (semitones % 12 + 12) % 12
            return entries.firstOrNull { it.semitones == norm } ?: UNISON
        }
    }
}
