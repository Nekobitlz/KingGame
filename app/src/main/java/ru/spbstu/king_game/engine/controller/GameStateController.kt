package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.vo.BaseFieldObject
import ru.spbstu.king_game.data.vo.CardStatus
import ru.spbstu.king_game.data.vo.CardVO
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.data.GameState
import ru.spbstu.king_game.view.utils.dpToPx

class GameStateController {

    private val _gameStateFlow = MutableStateFlow<GameState>(GameState.Prepared)
    val gameStateFlow get() = _gameStateFlow.asStateFlow()

    private val debugCards get() = mutableListOf<CardVO>().apply {
        repeat(8) { add(CardVO(CardSuit.values()[(0..3).random()], (6..14).random(), CardStatus.BACK, BaseFieldObject("$it", 0, 0,
            65.dpToPx.toInt(), 85.dpToPx.toInt()))) }
    }
    private val _fieldStateFlow = MutableStateFlow(FieldState(
        listOf(),
        mutableListOf<PlayerVO>().apply {
            repeat(4) {
                add(PlayerVO("$it", "Player$it", debugCards))
            }
        },
    ))
    val fieldStateFlow get() = _fieldStateFlow.asStateFlow()

    init {
        // todo get state from backend
        _gameStateFlow.tryEmit(GameState.Started)
    }

    fun onCardSelected(card: CardVO) {
        val value = _fieldStateFlow.value
        CoroutineScope(Dispatchers.Main).launch {
            _fieldStateFlow.emit(value.copy(deck = value.deck.toMutableList().apply {
                addAll(listOf(debugCards[0], debugCards[1], debugCards[2]))
            }))
        }

    }
}