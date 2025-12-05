package com.example.badart.model

import android.graphics.Bitmap

data class Post(
    val id: String,
    val artistName: String,
    val wordToGuess: String,
    val imageBitmap: Bitmap?,
    var isSolved: Boolean = false
)