package com.example.core.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.*

/**
 * Audio Beep Synthesizer for Satellite Signal Metering.
 * Adjusts pitch and beep intervals based on signal quality percentage (0-100%).
 */
class AudioFeedbackSynthesizer {

    private var toneGenerator: ToneGenerator? = null
    private var job: Job? = null
    private var isMuted = false

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stop()
        }
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Updates audio feedback according to signal quality.
     * @param qualityPercentage 0 to 100
     * @param isLocked whether transponder is locked
     */
    fun updateSignal(qualityPercentage: Int, isLocked: Boolean, scope: CoroutineScope) {
        if (isMuted || qualityPercentage <= 0) {
            stop()
            return
        }

        job?.cancel()
        job = scope.launch(Dispatchers.Default) {
            val intervalMs = when {
                qualityPercentage >= 90 -> 70L
                qualityPercentage >= 75 -> 140L
                qualityPercentage >= 50 -> 260L
                qualityPercentage >= 30 -> 450L
                else -> 800L
            }

            val toneType = when {
                isLocked -> ToneGenerator.TONE_PROP_BEEP2
                qualityPercentage >= 60 -> ToneGenerator.TONE_PROP_BEEP
                else -> ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE
            }

            while (isActive && !isMuted) {
                try {
                    toneGenerator?.startTone(toneType, 40)
                } catch (e: Exception) {
                    // Ignore transient audio issues
                }
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        try {
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        stop()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // Ignore
        }
        toneGenerator = null
    }
}
