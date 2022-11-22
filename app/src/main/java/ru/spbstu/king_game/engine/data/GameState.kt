package ru.spbstu.king_game.engine.data

sealed class GameState {
    object Prepared: GameState()
    object Started: GameState()
    object Paused: GameState()
    object Finished: GameState()
}