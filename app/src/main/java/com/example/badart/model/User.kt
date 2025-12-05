package com.example.badart.model

data class User(
    var userId: String = "",
    var username: String = "",
    var totalScore: Int = 0,
    var blockedUsers: MutableList<String> = mutableListOf()
)