package ru.spbstu.king_game.engine.repository

import ru.spbstu.king_game.engine.controller.DebugGameStateController
import ru.spbstu.king_game.engine.controller.GameStateController
import ru.spbstu.king_game.engine.controller.WebsocketGameStateController
import ru.spbstu.king_game.network.WebsocketService

object DependencyProvider {

    val currentUserRepository by lazy(LazyThreadSafetyMode.NONE) { CurrentUserRepository() }
    var gameStateController: GameStateController? = null

    fun createGameStateController() = WebsocketGameStateController(WebsocketService(), currentUserRepository)
}