package ru.spbstu.king_game.data.vo

import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.dto.card.Magnitude

class CardVO(
    val suit: CardSuit,
    val magnitude: Magnitude,
    var fieldState: BaseFieldObject,// = BaseFieldObject(110, 10, 200, 200),
)

enum class CardStatus {
    FACE, BACK
}