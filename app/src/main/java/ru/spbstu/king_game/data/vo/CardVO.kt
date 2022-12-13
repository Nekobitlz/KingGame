package ru.spbstu.king_game.data.vo

import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.dto.card.Magnitude

data class CardVO(
    val suit: CardSuit,
    val magnitude: Magnitude,
    var cardStatus: CardStatus = CardStatus.BACK,
    var fieldState: BaseFieldObject,
)

enum class CardStatus {
    FACE, BACK
}