package com.example.myapplication

object PlayerSession {

    var playerId: Long? = null
        private set

    var fullName: String = ""
        private set

    var difficulty: String = ""
        private set

    fun select(
        playerId: Long,
        fullName: String,
        difficulty: String
    ) {
        this.playerId = playerId
        this.fullName = fullName
        this.difficulty = difficulty
    }

    fun clear() {
        playerId = null
        fullName = ""
        difficulty = ""
    }
}
