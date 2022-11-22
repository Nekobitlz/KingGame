package ru.spbstu.king_game.data.vo

import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.dto.card.Magnitude

class CardVO(
    val suit: CardSuit,
    val magnitude: Magnitude,
    val cardStatus: CardStatus,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
): BaseFieldVO(x, y, width, height)

enum class CardStatus {
    FACE, BACK
}