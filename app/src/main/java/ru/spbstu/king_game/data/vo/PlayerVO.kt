package ru.spbstu.king_game.data.vo

data class PlayerVO(
    val id: String,
    val name: String,
    val cards: List<CardVO>,
)