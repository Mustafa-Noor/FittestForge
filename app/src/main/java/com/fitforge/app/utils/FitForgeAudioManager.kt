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

    fun init(context: Context) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()

        // Load short sounds into SoundPool
        // Note: These R.raw resource IDs must exist
        soundIds["tick"] = soundPool.load(context, R.raw.countdown_tick, 1)
        soundIds["beep"] = soundPool.load(context, R.raw.rest_timer_beep, 1)
        soundIds["badge"] = soundPool.load(context, R.raw.badge_unlock, 1)
        soundIds["up"] = soundPool.load(context, R.raw.momentum_up, 1)
    }

    fun playShort(key: String) {
        soundIds[key]?.let { soundPool.play(it, 1f, 1f, 0, 0, 1f) }
    }

    fun playWorkoutComplete(context: Context) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, R.raw.workout_complete)
        mediaPlayer?.start()
    }

    fun release() {
        mediaPlayer?.release()
        soundPool.release()
    }
}