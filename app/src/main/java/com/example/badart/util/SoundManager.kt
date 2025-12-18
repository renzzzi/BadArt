package com.example.badart.util

import android.content.Context
import android.media.SoundPool
import com.example.badart.R

object SoundManager {

    private var soundPool: SoundPool? = null

    private var correctGuessId: Int = 0
    private var wrongGuessId: Int = 0
    private var brushId: Int = 0
    private var fillId: Int = 0
    private var eraserId: Int = 0
    private var undoId: Int = 0
    private var redoId: Int = 0
    private var shareId: Int = 0
    private var clearDrawingId: Int = 0
    private var successModalId: Int = 0
    private var errorModalId: Int = 0
    private var submitDrawingId: Int = 0


    fun init(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        correctGuessId = soundPool?.load(context, R.raw.correct_guess, 1) ?: 0
        wrongGuessId = soundPool?.load(context, R.raw.wrong_guess, 1) ?: 0
        brushId = soundPool?.load(context, R.raw.brush, 1) ?: 0
        fillId = soundPool?.load(context, R.raw.fill, 1) ?: 0
        eraserId = soundPool?.load(context, R.raw.eraser, 1) ?: 0
        undoId = soundPool?.load(context, R.raw.undo, 1) ?: 0
        redoId = soundPool?.load(context, R.raw.redo, 1) ?: 0
        shareId = soundPool?.load(context, R.raw.share, 1) ?: 0
        clearDrawingId = soundPool?.load(context, R.raw.clear_drawing, 1) ?: 0
        successModalId = soundPool?.load(context, R.raw.success_modal, 1) ?: 0
        errorModalId = soundPool?.load(context, R.raw.error_modal, 1) ?: 0
        submitDrawingId = soundPool?.load(context, R.raw.submit_drawing, 1) ?: 0
    }

    fun playCorrectGuess() {
        soundPool?.play(correctGuessId, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    fun playWrongGuess() {
        soundPool?.play(wrongGuessId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playBrush() {
        soundPool?.play(brushId, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playFill() {
        soundPool?.play(fillId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playEraser() {
        soundPool?.play(eraserId, 0.3f, 0.3f, 1, 0, 1.0f)
    }

    fun playUndo() {
        soundPool?.play(undoId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playRedo() {
        soundPool?.play(redoId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playShare() {
        soundPool?.play(shareId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playDelete() {
        soundPool?.play(clearDrawingId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playSuccessModal() {
        soundPool?.play(successModalId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playErrorModal() {
        soundPool?.play(errorModalId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun playSubmitDrawing() {
        soundPool?.play(submitDrawingId, 0.6f, 0.6f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}