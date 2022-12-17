package ru.spbstu.king_game.data.vo.card

import ru.spbstu.king_game.data.dto.card.CardDTO
import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.dto.card.Magnitude
import ru.spbstu.king_game.data.vo.BaseFieldObject

data class CardVO(
    val suit: CardSuit,
    val magnitude: Magnitude,
    var cardStatus: CardStatus = CardStatus.BACK,
    var fieldState: BaseFieldObject,
) {
    companion object {
        fun mapFrom(cardDTO: CardDTO): CardVO = cardDTO.let {
            return CardVO(
                suit = it.suit,
                magnitude = it.magnitude,
                fieldState = BaseFieldObject.createEmpty(it.hashCode().toString())
            )
        }
    }
}

enum class CardStatus {
    FACE, BACK
}