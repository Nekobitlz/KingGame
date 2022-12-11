package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.vo.BaseFieldObject
import ru.spbstu.king_game.data.vo.CardVO
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.data.GameState
import ru.spbstu.king_game.view.utils.dpToPx
import java.util.*

class GameStateController {

    private val _gameStateFlow = MutableStateFlow<GameState>(GameState.Prepared)
    val gameStateFlow = _gameStateFlow.asStateFlow()

    private val debugCards get() = mutableListOf<CardVO>().apply {
        repeat(8) { add(CardVO(CardSuit.CLUBS, 6, BaseFieldObject("$it", 0, 0,
            65.dpToPx.toInt(), 85.dpToPx.toInt()))) }
    }
    private val _fieldStateFlow = MutableStateFlow(FieldState(LinkedList(),
        mutableListOf<PlayerVO>().apply {
            repeat(4) {
                add(PlayerVO("$it", "Player$it", debugCards))
            }
        }
    ))
    val fieldStateFlow = _fieldStateFlow.asStateFlow()

    init {
        // todo get state from backend
        _gameStateFlow.tryEmit(GameState.Started)
    }

    fun onCardSelected(card: CardVO): Unit? {
        TODO("Not yet implemented")
    }
}