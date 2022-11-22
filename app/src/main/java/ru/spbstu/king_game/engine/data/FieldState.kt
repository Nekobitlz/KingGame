package ru.spbstu.king_game.engine.data

import ru.spbstu.king_game.data.vo.CardVO
import ru.spbstu.king_game.data.vo.PlayerVO
import java.util.*

data class FieldState(
    val deck: LinkedList<CardVO>,
    val players: List<PlayerVO>,
)