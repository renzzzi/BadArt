package com.example.badart.util

object GameConstants {
    val WORDS = listOf(
        "Gravity", "Silence", "Shadow", "Reflection", "Dream",
        "Nightmare", "Headache", "Whisper", "Secret", "Magic",
        "Electricity", "Internet", "Bluetooth", "Virus", "Energy",
        "Memory", "Future", "History", "Music", "Noise",
        "Invisible", "Thunder", "Hunger", "Thirst", "Danger",

        "Confusion", "Jealousy", "Freedom", "Justice", "Peace",
        "Victory", "Defeat", "Boredom", "Surprise", "Panic",
        "Exhausted", "Healthy", "Dizzy", "Invisible", "Strong",

        "Airport", "Hospital", "Library", "Museum", "Circus",
        "Traffic", "Concert", "Prison", "Laboratory", "Factory",
        "University", "Cemetery", "Aquarium", "Stadium", "Theater",
        "Earthquake", "Tsunami", "Avalanche", "Tornado", "Volcano",

        "Dancing", "Swimming", "Fishing", "Camping", "Hiking",
        "Sneezing", "Coughing", "Sleeping", "Flying", "Falling",
        "Juggling", "Fighting", "Arguing", "Shopping", "Cooking",
        "Painting", "Writing", "Singing", "Driving", "Sailing",

        "Chandelier", "Typewriter", "Accordion", "Bagpipes", "Catapult",
        "Guillotine", "Satellite", "Telescope", "Microscope", "Compass",
        "Skeleton", "Scarecrow", "Mannequin", "Robot", "Cyborg",
        "Submarine", "Helicopter", "Elevator", "Escalator", "Treadmill",
        "Projector", "Generator", "Engine", "Battery", "Circuit",

        "Minotaur", "Werewolf", "Vampire", "Phoenix", "Griffin",
        "Mermaid", "Centaur", "Cyclops", "Dragon", "Unicorn",
        "Wizard", "Sorcerer", "Goblin", "Spirit", "Phantom",

        "Galaxy", "Universe", "Eclipse", "Meteor", "Nebula",
        "Glacier", "Iceberg", "Canyon", "Desert", "Jungle",
        "Bacteria", "Molecule", "Fossil", "Crystal", "Diamond",
        "Rainbow", "Lightning", "Horizon", "Sunset", "Sunrise",

        "Giraffe", "Elephant", "Kangaroo", "Penguin", "Dolphin",
        "Octopus", "Squirrel", "Hamster", "Gorilla", "Butterfly",
        "Mosquito", "Peacock", "Ostrich", "Raccoon", "Buffalo",
        "Leopard", "Cheetah", "Turtle", "Rabbit", "Spider",
        "Crocodile", "Flamingo", "Hedgehog", "Lobster", "Seahorse",
        "Vulture", "Walrus", "Zebra", "Parrot", "Chameleon",

        "Zombie", "Vampire", "Dragon", "Unicorn", "Alien",
        "Robot", "Wizard", "Ghost", "Mummy", "Mermaid",
        "Astronaut", "Spaceship", "Monster", "Goblin", "Werewolf",

        "Pizza", "Hamburger", "Sandwich", "Burrito", "Spaghetti",
        "Popcorn", "Pineapple", "Watermelon", "Strawberry", "Cupcake",
        "Pancake", "Broccoli", "Mushroom", "Avocado", "Coconut",
        "Pumpkin", "Cookie", "Donut", "Pretzel", "Waffle",

        "Toaster", "Camera", "Guitar", "Telescope", "Microscope",
        "Umbrella", "Backpack", "Suitcase", "Lantern", "Compass",
        "Scissors", "Hammer", "Ladder", "Shovel", "Whistle",
        "Computer", "Printer", "Keyboard", "Headphones", "Balloon",
        "Candle", "Anchor", "Magnet", "Clock", "Radio",

        "Helicopter", "Rocket", "Bicycle", "Submarine", "Ambulance",
        "Tractor", "Scooter", "Skateboard", "Sailboat", "Train",

        "Tornado", "Volcano", "Waterfall", "Mountain", "Island",
        "Cactus", "Flower", "Rainbow", "Lightning", "Snowflake",
        "Forest", "Desert", "Beach", "Thunder", "Cloud",

        "Pirate", "Ninja", "Clown", "Cowboy", "Dentist",
        "Doctor", "Teacher", "Skeleton", "Knight", "Princess",

        "Sweater", "Jacket", "Glasses", "Necklace", "Crown",
        "Helmet", "Boots", "Gloves", "Scarf", "Watch"
    )

    fun getRandomWord(): String {
        return WORDS.random()
    }
}