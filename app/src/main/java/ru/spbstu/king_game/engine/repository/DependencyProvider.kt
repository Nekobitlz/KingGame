package ru.spbstu.king_game.engine.repository

import ru.spbstu.king_game.engine.controller.DebugGameStateController
import ru.spbstu.king_game.engine.controller.GameStateController
import ru.spbstu.king_game.network.WebsocketService

object DependencyProvider {

    val currentUserRepository by lazy(LazyThreadSafetyMode.NONE) { CurrentUserRepository() }
    val websocketService by lazy(LazyThreadSafetyMode.NONE) { WebsocketService() }
    val gameStateController: GameStateController by lazy(LazyThreadSafetyMode.NONE) {
        DebugGameStateController()
        //WebsocketGameStateController(websocketService, currentUserRepository)
    }
}