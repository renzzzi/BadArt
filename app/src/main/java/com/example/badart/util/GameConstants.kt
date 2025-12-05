package com.example.badart.util

object GameConstants {
    val WORDS = listOf(
        "Toaster", "Giraffe", "Zombie", "Cactus", "Pizza",
        "Robot", "Ninja", "Unicorn", "Banana", "Vampire",
        "Kangaroo", "Tornado", "Pirate", "Dragon", "Hamburger",
        "Rocket", "Spider", "Six", "Skeleton", "Dinosaur",
        "Camera", "Guitar", "Helicopter", "Elephant", "Penguin",
        "Wizard", "Alien", "Seven", "Rainbow", "Butterfly"
    )

    fun getRandomWord(): String {
        return WORDS.random()
    }
}