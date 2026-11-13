package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Mars Outpost & NASA/JPL Quindar Telemetry Sound Synthesizer.
 * Generates 16-bit mono PCM waveforms at 22,050 Hz in real time using Android's AudioTrack API.
 * Zero external audio assets required; safe no-op fallback on JVM unit tests.
 */
enum class VaultSoundEffect(
    val title: String,
    val subtitle: String
) {
    PIP_BOY_CLICK(
        title = "Quindar Telemetry Beep",
        subtitle = "Crisp NASA/JPL 2525Hz->2475Hz mission control transponder chirp"
    ),
    GEIGER_TICK(
        title = "Cosmic Dosimeter & Laser Drill Click",
        subtitle = "Solar proton dosimeter tick and regolith core laser impulse"
    ),
    RESOURCE_CHIME(
        title = "MOXIE Resource & Gold Ore Chime",
        subtitle = "Rising tri-tone Martian telemetry harvest & Gold Ore chime"
    ),
    RUSH_SUCCESS(
        title = "Habitat Overclock Fanfare",
        subtitle = "4-note upbeat mission control bay overclock surge fanfare"
    ),
    KLAXON_ALARM(
        title = "Master Caution Dust-Storm Klaxon",
        subtitle = "Dual-tone emergency siren for O2 Fires, Dust Breaches, and Meteor Impacts"
    ),
    BLAST_DOOR_HYDRAULIC(
        title = "Airlock & Whipple Shield Seal",
        subtitle = "Pressurized Martian airlock equalization hiss and titanium bulkhead lock"
    ),
    INCIDENT_EXTINGUISH(
        title = "Halon Suppressant & Seal All-Clear",
        subtitle = "Halon fire suppressant burst and habitat pressure restored chime"
    )
}

object VaultSoundManager {
    const val SAMPLE_RATE_HZ = 22050

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isSfxEnabled = MutableStateFlow(true)
    val isSfxEnabled: StateFlow<Boolean> = _isSfxEnabled.asStateFlow()

    private val _isAmbientHumEnabled = MutableStateFlow(false)
    val isAmbientHumEnabled: StateFlow<Boolean> = _isAmbientHumEnabled.asStateFlow()

    private var ambientHumJob: Job? = null

    fun setSfxEnabled(enabled: Boolean) {
        _isSfxEnabled.value = enabled
        if (enabled) {
            play(VaultSoundEffect.PIP_BOY_CLICK)
        }
    }

    fun toggleSfx(): Boolean {
        val next = !_isSfxEnabled.value
        _isSfxEnabled.value = next
        if (next) {
            play(VaultSoundEffect.PIP_BOY_CLICK)
        }
        return next
    }

    fun toggleAmbientHum(): Boolean {
        val next = !_isAmbientHumEnabled.value
        _isAmbientHumEnabled.value = next
        if (next) {
            startAmbientHumLoop()
            play(VaultSoundEffect.BLAST_DOOR_HYDRAULIC)
        } else {
            stopAmbientHumLoop()
            play(VaultSoundEffect.PIP_BOY_CLICK)
        }
        return next
    }

    fun play(effect: VaultSoundEffect) {
        if (!_isSfxEnabled.value) return
        scope.launch {
            runCatching {
                val pcm = generatePcmSamples(effect)
                playPcmStatic(pcm, gain = 0.65f)
            }
        }
    }

    private fun startAmbientHumLoop() {
        ambientHumJob?.cancel()
        ambientHumJob = scope.launch {
            val humCycle = generateAmbientReactorHumPcm()
            while (isActive && _isAmbientHumEnabled.value) {
                runCatching {
                    playPcmStatic(humCycle, gain = 0.22f)
                }
                delay(1850L)
            }
        }
    }

    private fun stopAmbientHumLoop() {
        ambientHumJob?.cancel()
        ambientHumJob = null
    }

    /**
     * Pure deterministic/procedural PCM waveform generator.
     * Exposed as a public pure function so JVM unit tests can validate every sound effect.
     */
    fun generatePcmSamples(effect: VaultSoundEffect): ShortArray {
        return when (effect) {
            VaultSoundEffect.PIP_BOY_CLICK -> synthesizePipBoyClickPcm()
            VaultSoundEffect.GEIGER_TICK -> synthesizeGeigerTickPcm()
            VaultSoundEffect.RESOURCE_CHIME -> synthesizeResourceChimePcm()
            VaultSoundEffect.RUSH_SUCCESS -> synthesizeRushSuccessFanfarePcm()
            VaultSoundEffect.KLAXON_ALARM -> synthesizeKlaxonAlarmPcm()
            VaultSoundEffect.BLAST_DOOR_HYDRAULIC -> synthesizeBlastDoorHydraulicPcm()
            VaultSoundEffect.INCIDENT_EXTINGUISH -> synthesizeIncidentExtinguishPcm()
        }
    }

    private fun synthesizePipBoyClickPcm(): ShortArray {
        val durationMs = 50
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progress = i.toDouble() / totalSamples
            // Authentic NASA Quindar intro/outro telemetry step: 2525 Hz -> 2475 Hz
            val freq = if (progress < 0.5) 2525.0 else 2475.0
            val envelope = exp(-progress * 3.8)
            val wave = sin(2.0 * PI * freq * t) * 0.82 + sin(2.0 * PI * (freq * 0.5) * t) * 0.18
            out[i] = (wave * envelope * 21000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthesizeGeigerTickPcm(): ShortArray {
        val durationMs = 95
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        val rng = Random(76)
        // Three sharp Geiger-Mueller click impulses at 4ms, 34ms, and 68ms
        val pulseCenters = intArrayOf(
            (SAMPLE_RATE_HZ * 4) / 1000,
            (SAMPLE_RATE_HZ * 34) / 1000,
            (SAMPLE_RATE_HZ * 68) / 1000
        )
        for (i in 0 until totalSamples) {
            var sample = 0.0
            for (center in pulseCenters) {
                val dist = i - center
                if (dist in 0..140) {
                    val decay = exp(-dist / 24.0)
                    val burst = (rng.nextDouble(-1.0, 1.0) * 0.65 + sin(2.0 * PI * 2400.0 * (dist.toDouble() / SAMPLE_RATE_HZ)) * 0.35)
                    sample += burst * decay
                }
            }
            out[i] = (sample.coerceIn(-1.0, 1.0) * 24000.0).toInt().toShort()
        }
        return out
    }

    private fun synthesizeResourceChimePcm(): ShortArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99) // C5 -> E5 -> G5
        val noteDurationMs = 70
        val samplesPerNote = (SAMPLE_RATE_HZ * noteDurationMs) / 1000
        val out = ShortArray(samplesPerNote * notes.size)
        for (n in notes.indices) {
            val freq = notes[n]
            val offset = n * samplesPerNote
            for (i in 0 until samplesPerNote) {
                val t = i.toDouble() / SAMPLE_RATE_HZ
                val progress = i.toDouble() / samplesPerNote
                val envelope = (1.0 - progress * 0.65)
                val sample = (sin(2.0 * PI * freq * t) * 0.75 + sin(2.0 * PI * freq * 2.0 * t) * 0.25) * envelope
                out[offset + i] = (sample * 20000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return out
    }

    private fun synthesizeRushSuccessFanfarePcm(): ShortArray {
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5 -> E5 -> G5 -> C6
        val noteDurationMs = 75
        val samplesPerNote = (SAMPLE_RATE_HZ * noteDurationMs) / 1000
        val out = ShortArray(samplesPerNote * notes.size)
        for (n in notes.indices) {
            val freq = notes[n]
            val offset = n * samplesPerNote
            for (i in 0 until samplesPerNote) {
                val t = i.toDouble() / SAMPLE_RATE_HZ
                val progress = i.toDouble() / samplesPerNote
                val envelope = exp(-progress * 1.4)
                val harmonic = sin(2.0 * PI * freq * t) * 0.70 +
                    sin(2.0 * PI * (freq * 1.5) * t) * 0.15 +
                    sin(2.0 * PI * (freq * 2.0) * t) * 0.15
                out[offset + i] = (harmonic * envelope * 22000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return out
    }

    private fun synthesizeKlaxonAlarmPcm(): ShortArray {
        val durationMs = 420
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progress = i.toDouble() / totalSamples
            // Dual-tone siren sweep: 440Hz up to 640Hz then back
            val baseFreq = if (progress < 0.5) {
                440.0 + (progress * 2.0) * 200.0
            } else {
                640.0 - ((progress - 0.5) * 2.0) * 180.0
            }
            val rawWave = sin(2.0 * PI * baseFreq * t) * 0.65 +
                sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.25 +
                sin(2.0 * PI * (baseFreq * 3.0) * t) * 0.10
            val env = if (progress > 0.85) (1.0 - (progress - 0.85) / 0.15) else 1.0
            out[i] = (rawWave * env * 23000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthesizeBlastDoorHydraulicPcm(): ShortArray {
        val durationMs = 340
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        val rng = Random(101)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progress = i.toDouble() / totalSamples
            // Phase 1 (0..65%): pressurized pneumatic steam hiss + servo whine
            // Phase 2 (65..100%): deep 82Hz steel bulkhead seal thud
            val hissEnv = if (progress < 0.65) (1.0 - (progress / 0.65) * 0.5) else exp(-(progress - 0.65) * 12.0)
            val hiss = rng.nextDouble(-1.0, 1.0) * 0.38 * hissEnv
            val servoFreq = 310.0 - 150.0 * progress
            val servo = sin(2.0 * PI * servoFreq * t) * 0.28 * hissEnv
            val thudEnv = if (progress >= 0.58) exp(-(progress - 0.58) * 7.5) else 0.0
            val thud = sin(2.0 * PI * 82.0 * t) * 0.85 * thudEnv
            val combined = (hiss + servo + thud).coerceIn(-1.0, 1.0)
            out[i] = (combined * 23000.0).toInt().toShort()
        }
        return out
    }

    private fun synthesizeIncidentExtinguishPcm(): ShortArray {
        val durationMs = 280
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        val rng = Random(88)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progress = i.toDouble() / totalSamples
            // CO2 foam spray + rising all-clear tone (587Hz -> 880Hz)
            val sprayEnv = exp(-progress * 3.8)
            val spray = rng.nextDouble(-1.0, 1.0) * 0.42 * sprayEnv
            val chimeFreq = if (progress < 0.5) 587.33 else 880.0
            val chime = sin(2.0 * PI * chimeFreq * t) * 0.55 * (1.0 - progress * 0.4)
            val combined = (spray + chime).coerceIn(-1.0, 1.0)
            out[i] = (combined * 21000.0).toInt().toShort()
        }
        return out
    }

    fun generateAmbientReactorHumPcm(): ShortArray {
        val durationMs = 1800
        val totalSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val out = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE_HZ
            val progress = i.toDouble() / totalSamples
            // Smooth fade-in and fade-out envelope so loops are click-free
            val fade = when {
                progress < 0.08 -> progress / 0.08
                progress > 0.92 -> (1.0 - progress) / 0.08
                else -> 1.0
            }
            val lfo = 0.85 + 0.15 * sin(2.0 * PI * 1.1 * t)
            val wave = (sin(2.0 * PI * 60.0 * t) * 0.55 +
                sin(2.0 * PI * 120.0 * t) * 0.30 +
                sin(2.0 * PI * 180.0 * t) * 0.15) * lfo * fade
            out[i] = (wave * 12000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private suspend fun playPcmStatic(samples: ShortArray, gain: Float) {
        if (samples.isEmpty()) return
        val scaled = ShortArray(samples.size) { idx ->
            (samples[idx] * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        val byteCount = scaled.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE_HZ)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteCount.coerceAtLeast(2048))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(scaled, 0, scaled.size)
            track.play()
            val durationMs = ((scaled.size.toLong() * 1000L) / SAMPLE_RATE_HZ) + 35L
            delay(durationMs)
        } finally {
            runCatching {
                track.stop()
                track.release()
            }
        }
    }
}
