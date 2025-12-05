package com.example.badart.model

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude

data class Post(
    var id: String = "",
    var artistName: String = "",
    var wordToGuess: String = "",

    var isSolved: Boolean = false,

    var winner: String = "",
    var guessHistory: MutableList<String> = mutableListOf(),
    var reportCount: Int = 0,
    var timestamp: Long = 0,

    var imageBase64: String = "",
    @get:Exclude var imageBitmap: Bitmap? = null
)