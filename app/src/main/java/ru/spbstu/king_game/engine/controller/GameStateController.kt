package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.flow.StateFlow
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.data.vo.game.GameStateVO

interface GameStateController {
    val gameStateFlow: StateFlow<GameStateVO>

    fun onCardSelected(card: CardVO)
}