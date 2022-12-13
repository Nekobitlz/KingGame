package ru.spbstu.king_game.data.vo

data class PlayerVO(
    val id: String,
    val name: String,
    var cards: List<CardVO>,
)