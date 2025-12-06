package com.example.badart.util

import android.content.Context
import android.media.SoundPool
import com.example.badart.R

object SoundManager {

    private var soundPool: SoundPool? = null
    private var tapSound: Int = 0
    private var successSound: Int = 0
    private var errorSound: Int = 0
    private var trophySound: Int = 0
    private var notificationSound: Int = 0
    private var brushSwitchSound: Int = 0
    private var eraserSound: Int = 0
    private var undoSound: Int = 0
    private var redoSound: Int = 0
    private var shareSound: Int = 0
    private var deleteSound: Int = 0

    fun init(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        tapSound = soundPool?.load(context, R.raw.tap, 1) ?: 0
        successSound = soundPool?.load(context, R.raw.success, 1) ?: 0
        errorSound = soundPool?.load(context, R.raw.error, 1) ?: 0
        trophySound = soundPool?.load(context, R.raw.trophy, 1) ?: 0
        notificationSound = soundPool?.load(context, R.raw.notification, 1) ?: 0
        brushSwitchSound = soundPool?.load(context, R.raw.brush_switch, 1) ?: 0
        eraserSound = soundPool?.load(context, R.raw.eraser, 1) ?: 0
        undoSound = soundPool?.load(context, R.raw.undo, 1) ?: 0
        redoSound = soundPool?.load(context, R.raw.redo, 1) ?: 0
        shareSound = soundPool?.load(context, R.raw.share, 1) ?: 0
        deleteSound = soundPool?.load(context, R.raw.delete, 1) ?: 0
    }

    fun playTap() {
        soundPool?.play(tapSound, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playSuccess() {
        soundPool?.play(successSound, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    fun playError() {
        soundPool?.play(errorSound, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playTrophy() {
        soundPool?.play(trophySound, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playNotification() {
        soundPool?.play(notificationSound, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playBrushSwitch() {
        soundPool?.play(brushSwitchSound, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playEraser() {
        soundPool?.play(eraserSound, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playUndo() {
        soundPool?.play(undoSound, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playRedo() {
        soundPool?.play(redoSound, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playShare() {
        soundPool?.play(shareSound, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playDelete() {
        soundPool?.play(deleteSound, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}