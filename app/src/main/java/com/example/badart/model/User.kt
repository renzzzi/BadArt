package com.example.badart.model

import com.google.firebase.firestore.Exclude

data class User(
    var userId: String = "",
    var username: String = "",
    var totalScore: Int = 0,
    var correctGuesses: Int = 0,
    var postCount: Int = 0,
    var avatarBase64: String = "",
    var blockedUsers: MutableList<String> = mutableListOf(),
    var reportedPosts: MutableList<String> = mutableListOf()
)