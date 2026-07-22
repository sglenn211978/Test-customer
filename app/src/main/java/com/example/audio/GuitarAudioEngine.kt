package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance real-time guitar audio synthesis engine.
 * Uses Karplus-Strong physical modeling algorithm to synthesize authentic plucked guitar string sound with body resonance.
 * Supports tap trigger, pitch pipe sustaining tuner mode, scale arpeggio playback, and ear training notes.
 */
class GuitarAudioEngine {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)
    private var pitchPipeJob: Job? = null
    private var pitchPipeTrack: AudioTrack? = null

    /**
     * Synthesize and play a plucked guitar note using Karplus-Strong physical modeling.
     * @param frequencyHz frequency of the note
     * @param durationSeconds duration of the ring out (default 2.0s)
     */
    fun playPluckedNote(frequencyHz: Float, durationSeconds: Float = 2.0f) {
        scope.launch {
            try {
                val numSamples = (sampleRate * durationSeconds).toInt()
                val bufferLength = (sampleRate / frequencyHz).toInt().coerceAtLeast(10)
                
                // Karplus-Strong delay line initialized with filtered noise (pluck transient)
                val delayLine = FloatArray(bufferLength)
                val random = Random()
                for (i in delayLine.indices) {
                    // Slight high-frequency noise burst for pluck attack
                    delayLine[i] = (random.nextFloat() * 2.0f - 1.0f)
                }

                val pcmData = ShortArray(numSamples)
                var delayIndex = 0
                var previousSample = 0.0f
                val decayFactor = 0.992f // Decay rate for guitar string sustain

                for (n in 0 until numSamples) {
                    val currentSample = delayLine[delayIndex]
                    
                    // Low-pass filter (averaging adjacent samples) + decay
                    val nextSample = (currentSample + previousSample) * 0.5f * decayFactor
                    delayLine[delayIndex] = nextSample
                    previousSample = currentSample

                    // Apply gentle exponential envelope for smooth decay
                    val env = exp(-3.0 * n / numSamples).toFloat()
                    val output = (currentSample * env * 32000.0f).coerceIn(-32767.0f, 32767.0f)
                    
                    pcmData[n] = output.toInt().toShort()

                    delayIndex = (delayIndex + 1) % bufferLength
                }

                // Play PCM buffer using AudioTrack
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmData, 0, pcmData.size)
                track.play()

                // Release track after playback completes
                delay((durationSeconds * 1000).toLong() + 200)
                track.release()
            } catch (e: Exception) {
                Log.e("GuitarAudioEngine", "Error playing plucked note", e)
            }
        }
    }

    /**
     * Start sustaining pitch pipe tone for reference tuning.
     */
    fun startPitchPipe(frequencyHz: Float) {
        stopPitchPipe()
        pitchPipeJob = scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                pitchPipeTrack = track
                track.play()

                val buffer = ShortArray(minBufferSize)
                var phase = 0.0

                while (pitchPipeTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    for (i in buffer.indices) {
                        // Complex sine with warm harmonics (fundamental + 2nd + 3rd harmonic)
                        val fundamental = sin(2.0 * PI * phase)
                        val harmonic2 = 0.3 * sin(4.0 * PI * phase)
                        val harmonic3 = 0.15 * sin(6.0 * PI * phase)
                        
                        val sampleValue = ((fundamental + harmonic2 + harmonic3) * 0.6 * 30000.0).coerceIn(-32767.0, 32767.0)
                        buffer[i] = sampleValue.toInt().toShort()

                        phase += frequencyHz / sampleRate
                        if (phase >= 1.0) phase -= 1.0
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e("GuitarAudioEngine", "Pitch pipe error", e)
            }
        }
    }

    /**
     * Stop pitch pipe tone.
     */
    fun stopPitchPipe() {
        try {
            pitchPipeTrack?.stop()
            pitchPipeTrack?.release()
            pitchPipeTrack = null
            pitchPipeJob?.cancel()
            pitchPipeJob = null
        } catch (e: Exception) {
            Log.e("GuitarAudioEngine", "Error stopping pitch pipe", e)
        }
    }

    /**
     * Play a sequence of frequencies as an arpeggio (e.g., scale or chord play-through).
     */
    fun playArpeggio(frequenciesHz: List<Float>, delayBetweenNotesMs: Long = 250) {
        scope.launch {
            for (freq in frequenciesHz) {
                playPluckedNote(freq, 1.5f)
                delay(delayBetweenNotesMs)
            }
        }
    }

    fun release() {
        stopPitchPipe()
    }
}
