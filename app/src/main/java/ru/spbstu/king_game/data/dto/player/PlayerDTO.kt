package ru.spbstu.king_game.data.dto.player

import ru.spbstu.king_game.data.dto.card.CardDTO

data class PlayerDTO(
    val id: String,
    val name: String,
    val cards: List<CardDTO>,
)