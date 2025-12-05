package com.example.badart.model

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude

data class Post(
    var id: String = "",
    var artistName: String = "",
    var wordToGuess: String = "",
    var imageBase64: String = "",
    var isSolved: Boolean = false,
    var reportCount: Int = 0,
    var timestamp: Long = 0,
    @get:Exclude var imageBitmap: Bitmap? = null
)