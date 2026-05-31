package com.fitforge.app.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.fitforge.app.R

object FitForgeAudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var soundPool: SoundPool
    private var soundIds = mutableMapOf<String, Int>()
    private var initialized = false

    fun init(context: Context) {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()

            // Load short sounds into SoundPool - gracefully handle missing resources
            try { soundIds["tick"] = soundPool.load(context, R.raw.countdown_tick, 1) } catch (e: Exception) { }
            try { soundIds["beep"] = soundPool.load(context, R.raw.rest_timer_beep, 1) } catch (e: Exception) { }
            try { soundIds["badge"] = soundPool.load(context, R.raw.badge_unlock, 1) } catch (e: Exception) { }
            try { soundIds["up"] = soundPool.load(context, R.raw.momentum_up, 1) } catch (e: Exception) { }
            
            initialized = true
        } catch (e: Exception) {
            // If SoundPool initialization fails, gracefully degrade
            initialized = false
        }
    }

    fun playShort(key: String) {
        if (!initialized) return
        try {
            soundIds[key]?.let { soundPool.play(it, 1f, 1f, 0, 0, 1f) }
        } catch (e: Exception) {
            // Silently fail if sound playback fails
        }
    }

    fun playWorkoutComplete(context: Context) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, R.raw.workout_complete)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Silently fail if audio file doesn't exist
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            if (initialized) {
                soundPool.release()
            }
        } catch (e: Exception) {
            // Silently fail on cleanup
        }
    }
}