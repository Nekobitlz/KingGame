package ru.spbstu.king_game.data.vo.card

import androidx.annotation.DrawableRes
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.dto.card.isValid

object CardMapper {

    private val heartsList = listOf(
        R.drawable.hearts_6, R.drawable.hearts_7, R.drawable.hearts_8,
        R.drawable.hearts_9, R.drawable.hearts_10, R.drawable.valet_hearts,
        R.drawable.queen_hearts, R.drawable.king_hearts, R.drawable.tuz_hearts,
    )

    private val diamondsList = listOf(
        R.drawable.diamonds_6, R.drawable.diamonds_7,
        R.drawable.diamonds_8, R.drawable.diamonds_9, R.drawable.diamonds_10,
        R.drawable.valet_diamonds, R.drawable.queen_diamonds, R.drawable.king_diamonds, R.drawable.tuz_diamonds
    )

    private val clubsList = listOf(
        R.drawable.clubs_6, R.drawable.clubs_7, R.drawable.clubs_8,
        R.drawable.clubs_9, R.drawable.clubs_10, R.drawable.valet_clubs,
        R.drawable.queen_clubs, R.drawable.king_clubs, R.drawable.tuz_clubs,
    )

    private val spadesList = listOf(
        R.drawable.spades_6, R.drawable.spades_7, R.drawable.spades_8,
        R.drawable.spades_9, R.drawable.spades_10, R.drawable.valet_spades,
        R.drawable.queen_spades, R.drawable.king_spades, R.drawable.tuz_spades,
    )

    @DrawableRes
    fun mapFrom(cardVO: CardVO): Int {
        val list = when (cardVO.suit) {
            CardSuit.hearts -> heartsList
            CardSuit.clubs -> clubsList
            CardSuit.diamonds -> diamondsList
            CardSuit.spades -> spadesList
        }
        if (cardVO.magnitude.isValid()) {
            return list[cardVO.magnitude - 6]
        }
        return -1
    }
}