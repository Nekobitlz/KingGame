package ru.spbstu.king_game.engine.data

import ru.spbstu.king_game.data.vo.CardVO
import ru.spbstu.king_game.data.vo.PlayerVO

data class FieldState(
    val deck: List<CardVO>,
    val players: List<PlayerVO>,
    var currentStep: PlayerVO = players[0],
    var currentMode: String = "Не брать взятки"
) {
 }