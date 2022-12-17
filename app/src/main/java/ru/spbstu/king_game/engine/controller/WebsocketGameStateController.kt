package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import ru.spbstu.king_game.data.dto.card.CardDTO
import ru.spbstu.king_game.data.dto.game.SimpleGameStateDTO
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.engine.data.Action
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.network.WebsocketRequest
import ru.spbstu.king_game.network.WebsocketResponse
import ru.spbstu.king_game.network.WebsocketService

class WebsocketGameStateController(
    private val websocketService: WebsocketService,
    private val currentUserRepository: CurrentUserRepository,
) : GameStateController {

    private val _gameStateFlow = MutableStateFlow<GameStateVO>(GameStateVO.NotInitialized)
    override val gameStateFlow: StateFlow<GameStateVO>
        get() = _gameStateFlow.asStateFlow()

    private var gameSession = 0
    private val playerName = "Сашок" // todo

    init {
        websocketService.observeEvent()
            .onEach {
                when (it) {
                    is WebsocketResponse.UpdateGameState -> {
                        it.gameState.players?.firstOrNull()?.let { player ->
                            currentUserRepository.setCurrentUser(PlayerVO.mapFrom(player))
                        }
                        _gameStateFlow.emit(GameStateVO.mapFrom(it.gameState))
                    }
                    is WebsocketResponse.OpenSession -> {
                        gameSession = it.sessionId
                        websocketService.sendCommand(WebsocketRequest.StartGame(
                            gameSession, playerName, Action.play
                        ))
                    }
                }
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    override fun onCardSelected(card: CardVO) {
        val simpleState = _gameStateFlow.value as? GameStateVO.Started ?: return
        websocketService.sendCommand(
            WebsocketRequest.SendAction(
                gameSession, currentUserRepository.currentUserId.orEmpty(),
                Action.turn,
                SimpleGameStateDTO(simpleState.gameNum, simpleState.circleNum),
                turn = CardDTO(card.suit, card.magnitude)
            )
        )
    }
}