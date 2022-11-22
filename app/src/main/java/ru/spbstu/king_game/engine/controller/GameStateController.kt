package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.data.GameState
import java.util.*

class GameStateController {

    private val _stateSharedFlow = MutableStateFlow<GameState>(GameState.Prepared)
    val stateSharedFlow = _stateSharedFlow.asStateFlow()

    private val _fieldStateSharedFlow = MutableStateFlow(FieldState(LinkedList(), listOf()))
    val fieldStateSharedFlow = _fieldStateSharedFlow.asStateFlow()

    init {
        // todo get state from backend
        _stateSharedFlow.tryEmit(GameState.Started)
    }
}