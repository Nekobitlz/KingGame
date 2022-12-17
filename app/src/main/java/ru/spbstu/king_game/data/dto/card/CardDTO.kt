package ru.spbstu.king_game.data.dto.card

@kotlinx.serialization.Serializable
data class CardDTO(
    val suit: CardSuit,
    val magnitude: Magnitude
)



