package com.example.badart.model

data class Post(
    val id: String,
    val artistName: String,
    val wordToGuess: String,
    val imageResId: Int,
    var isSolved: Boolean = false
)