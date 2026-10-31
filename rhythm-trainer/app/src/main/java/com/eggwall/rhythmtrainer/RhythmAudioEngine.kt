package com.eggwall.rhythmtrainer

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Synthesizes and plays the 4..3..2..1 countdown clicks followed by the single-frequency
 * rhythm notes and rests based on their musical duration.
 */
class RhythmAudioEngine(
    private val scope: CoroutineScope
) {
    companion object {
        const val SAMPLE_RATE = 44100
        /** Single fixed frequency (Concert A4 = 440 Hz) for all rhythm notes and clicks. */
        const val FIXED_FREQUENCY_HZ = 440.0
        const val COUNTDOWN_START = 4

        /**
         * Computes the duration in milliseconds of one sixteenth-note unit at [bpm]
         * (where [bpm] is quarter-note beats per minute).
         */
        fun sixteenthDurationMs(bpm: Int): Double {
            val safeBpm = bpm.coerceIn(30, 240)
            val quarterDurationMs = 60_000.0 / safeBpm
            return quarterDurationMs / 4.0
        }

        /**
         * Synthesizes a 16-bit mono PCM buffer for either a countdown click or a rhythm note/rest.
         *
         * - Countdown clicks: sharp percussive click (~45 ms) at [FIXED_FREQUENCY_HZ] followed by silence
         *   for the remainder of the beat.
         * - Notes: sharp click attack transient (~25 ms) at [FIXED_FREQUENCY_HZ] followed by a sustained
         *   single-frequency tone that holds for 85% of the note's duration and releases cleanly over the
         *   final 15% so both the onset click and the exact note duration are clearly audible.
         * - Rests: pure silence for the entire duration.
         */
        fun synthesizeEventPcm(
            totalSamples: Int,
            isRest: Boolean,
            isCountdownClick: Boolean,
            sampleRate: Int = SAMPLE_RATE,
            frequencyHz: Double = FIXED_FREQUENCY_HZ
        ): ShortArray {
            val buffer = ShortArray(totalSamples.coerceAtLeast(1))
            if (isRest || totalSamples <= 0) {
                return buffer
            }

            if (isCountdownClick) {
                val clickSamples = min(totalSamples, (sampleRate * 0.050).toInt())
                val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)
                for (i in 0 until clickSamples) {
                    val t = i.toDouble() / sampleRate
                    val attackEnv = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                    val decayEnv = exp(-t * 55.0)
                    val sample = sin(2.0 * PI * frequencyHz * t) * attackEnv * decayEnv
                    buffer[i] = (sample * Short.MAX_VALUE * 0.90).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }
                return buffer
            }

            // Rhythm note: crisp click onset + sustained body proportional to note duration + articulation gap
            val articulationGapSamples = min(
                (totalSamples * 0.14).toInt(),
                (sampleRate * 0.045).toInt()
            ).coerceAtLeast((sampleRate * 0.012).toInt())
            val activeSamples = (totalSamples - articulationGapSamples).coerceAtLeast(1)
            val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)
            val releaseSamples = min((activeSamples * 0.15).toInt(), (sampleRate * 0.025).toInt()).coerceAtLeast(1)

            for (i in 0 until activeSamples) {
                val t = i.toDouble() / sampleRate
                val attackEnv = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                val releaseEnv = if (i > activeSamples - releaseSamples) {
                    (activeSamples - i).toDouble() / releaseSamples
                } else {
                    1.0
                }
                // Extra onset click boost in the first 25 ms so every note starts with a clear click
                val clickBoost = 0.45 * exp(-t * 65.0)
                val sustainLevel = 0.50
                val envelope = attackEnv * releaseEnv * (sustainLevel + clickBoost)
                val wave = sin(2.0 * PI * frequencyHz * t)
                val value = (wave * envelope * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = value.toShort()
            }
            return buffer
        }
    }

    private var playbackJob: Job? = null

    fun startPlayback(
        measures: List<Measure>,
        bpm: Int,
        loop: Boolean,
        onCountdownTick: (Int?) -> Unit,
        onActiveElementChanged: (ElementPosition?) -> Unit,
        onPlaybackCompleted: () -> Unit
    ) {
        stopPlayback()
        playbackJob = scope.launch(Dispatchers.Default) {
            val minBufferBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                audioTrack.play()

                val sixteenthMs = sixteenthDurationMs(bpm)
                val quarterSamples = ((sixteenthMs * 4.0 / 1000.0) * SAMPLE_RATE).toInt().coerceAtLeast(1)

                // Phase 1: Countdown 4 .. 3 .. 2 .. 1 using clicks
                for (count in COUNTDOWN_START downTo 1) {
                    if (!isActive) break
                    withContext(Dispatchers.Main) {
                        onActiveElementChanged(null)
                        onCountdownTick(count)
                    }
                    val clickPcm = synthesizeEventPcm(
                        totalSamples = quarterSamples,
                        isRest = false,
                        isCountdownClick = true
                    )
                    writePcmChunked(audioTrack, clickPcm)
                }

                withContext(Dispatchers.Main) {
                    onCountdownTick(null)
                }

                // Phase 2: Play rhythm measures according to each note/rest duration
                do {
                    for ((measureIndex, measure) in measures.withIndex()) {
                        if (!isActive) break
                        for ((elementIndex, element) in measure.elements.withIndex()) {
                            if (!isActive) break
                            val pos = ElementPosition(measureIndex, elementIndex)
                            withContext(Dispatchers.Main) {
                                onActiveElementChanged(pos)
                            }
                            val durationMs = element.duration.sixteenthUnits * sixteenthMs
                            val totalSamples = ((durationMs / 1000.0) * SAMPLE_RATE).toInt().coerceAtLeast(1)
                            val pcm = synthesizeEventPcm(
                                totalSamples = totalSamples,
                                isRest = element.isRest,
                                isCountdownClick = false
                            )
                            writePcmChunked(audioTrack, pcm)
                        }
                    }
                } while (isActive && loop)

                if (isActive) {
                    withContext(Dispatchers.Main) {
                        onCountdownTick(null)
                        onActiveElementChanged(null)
                        onPlaybackCompleted()
                    }
                }
            } finally {
                runCatching {
                    audioTrack.pause()
                    audioTrack.flush()
                    audioTrack.release()
                }
            }
        }
    }

    private fun kotlinx.coroutines.CoroutineScope.writePcmChunked(
        audioTrack: AudioTrack,
        pcm: ShortArray
    ) {
        val chunkSize = 882 // ~20ms at 44.1kHz for responsive pause/stop
        var offset = 0
        while (offset < pcm.size && isActive) {
            val count = min(chunkSize, pcm.size - offset)
            val written = audioTrack.write(pcm, offset, count)
            if (written <= 0) break
            offset += written
        }
    }

    fun stopPlayback() {
        val job = playbackJob
        playbackJob = null
        job?.cancel()
    }

    suspend fun stopAndJoin() {
        val job = playbackJob
        playbackJob = null
        job?.cancelAndJoin()
    }
}
