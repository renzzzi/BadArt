package com.example.badart.model

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Post(
    var id: String = "",
    var artistName: String = "",
    var artistId: String = "",
    var wordToGuess: String = "",

    @get:PropertyName("isSolved") @set:PropertyName("isSolved")
    var isSolved: Boolean = false,

    var winner: String = "",
    var guessHistory: MutableList<String> = mutableListOf(),
    var reportCount: Int = 0,
    var timestamp: Long = 0,
    var reactions: MutableMap<String, Int> = mutableMapOf(),
    var userReactions: MutableMap<String, String> = mutableMapOf(),

    var imageBase64: String = "",
    @get:Exclude var imageBitmap: Bitmap? = null
)