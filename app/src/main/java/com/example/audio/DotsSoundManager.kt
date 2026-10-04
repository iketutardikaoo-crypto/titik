package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural audio synthesizer and haptics manager for Dots.
 * Produces crisp, beautiful musical chimes that pitch-escalate as dot chains grow.
 */
class DotsSoundManager(private val context: Context) {

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Musical Pentatonic Scale frequencies (Hz) for ascending dot connections
    private val pentatonicScale = listOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f, // A5
        1046.50f, // C6
        1174.66f, // D6
        1318.51f  // E6
    )

    /**
     * Play musical chime for dot connection at given chain index.
     * Chain index 0 -> C4, index 1 -> D4, etc.
     */
    fun playDotConnect(chainIndex: Int) {
        if (!soundEnabled) return
        val freqIndex = (chainIndex).coerceIn(0, pentatonicScale.lastIndex)
        val frequency = pentatonicScale[freqIndex]
        playTone(frequency, durationMs = 120, attackMs = 15, decayMs = 95, volume = 0.6f)
    }

    /**
     * Play harmonic fanfare chord when a closed loop / square is completed.
     */
    fun playLoopCelebration() {
        if (!soundEnabled) return
        audioScope.launch {
            // Play C5, E5, G5, C6 chord combined
            playChord(listOf(523.25f, 659.25f, 783.99f, 1046.50f), durationMs = 380, volume = 0.7f)
        }
    }

    /**
     * Play popping sound when dots clear.
     */
    fun playPop() {
        if (!soundEnabled) return
        playSweep(startFreq = 800f, endFreq = 400f, durationMs = 80, volume = 0.45f)
    }

    /**
     * Play power-up burst sound.
     */
    fun playBlast() {
        if (!soundEnabled) return
        playSweep(startFreq = 300f, endFreq = 1100f, durationMs = 180, volume = 0.55f)
    }

    /**
     * Subtle haptic feedback when connecting a dot.
     */
    fun vibrateDotConnect() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }

    /**
     * Strong haptic pulse when a closed loop / square is made.
     */
    fun vibrateLoopMade() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 60), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(60)
        }
    }

    /**
     * Haptic feedback when claiming a box in Dots & Boxes.
     */
    fun vibrateBoxClaimed() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    private fun playTone(frequency: Float, durationMs: Int, attackMs: Int, decayMs: Int, volume: Float) {
        audioScope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)

                val attackSamples = (sampleRate * (attackMs / 1000f)).toInt()
                val decaySamples = numSamples - attackSamples

                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                    val envelope = if (i < attackSamples) {
                        i.toFloat() / attackSamples
                    } else {
                        val decayProgress = (i - attackSamples).toFloat() / decaySamples
                        (1f - decayProgress).coerceIn(0f, 1f)
                    }
                    val sample = (sin(angle) * envelope * volume * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                writeAndPlay(buffer, sampleRate)
            } catch (_: Exception) {
                // Ignore audio play errors
            }
        }
    }

    private fun playChord(frequencies: List<Float>, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                var totalSin = 0.0
                for (f in frequencies) {
                    totalSin += sin(2.0 * Math.PI * i / (sampleRate / f))
                }
                totalSin /= frequencies.size

                val envelope = 1f - (i.toFloat() / numSamples)
                val sample = (totalSin * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer, sampleRate)
        } catch (_: Exception) {
            // Ignore audio play errors
        }
    }

    private fun playSweep(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        audioScope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    val angle = 2.0 * Math.PI * i / (sampleRate / currentFreq)
                    val envelope = 1f - progress
                    val sample = (sin(angle) * envelope * volume * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                writeAndPlay(buffer, sampleRate)
            } catch (_: Exception) {
                // Ignore audio play errors
            }
        }
    }

    private fun writeAndPlay(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        // Clean up after playback
        audioScope.launch {
            kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 50)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
