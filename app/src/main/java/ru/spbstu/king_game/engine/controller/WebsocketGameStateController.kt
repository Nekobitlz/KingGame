package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import ru.spbstu.king_game.data.dto.card.CardDTO
import ru.spbstu.king_game.data.dto.game.SimpleGameStateDTO
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.data.vo.game.orEmpty
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

    private var sessionId: String? = null
    private var gameSessionId = 0L
    private val playerName = currentUserRepository.currentName ?: "Player"

    init {
        websocketService.observeEvent()
            .onEach {
                when (it) {
                    is WebsocketResponse.UpdateGameState -> {
                        gameSessionId = it.gameSessionId
                        it.gameState.players?.firstOrNull()?.let { player ->
                            currentUserRepository.setCurrentUser(PlayerVO.mapFrom(player))
                        }
                        _gameStateFlow.emit(GameStateVO.mapFrom(it.gameState))
                    }
                    is WebsocketResponse.OpenSession -> {
                        sessionId = it.sessionId
                        websocketService.sendCommand(WebsocketRequest.StartGame(
                            sessionId!!, playerName, Action.play
                        ))
                    }
                    is WebsocketResponse.Error -> {
                        _gameStateFlow.emit(GameStateVO.Error(it.errorType))
                    }
                }
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    override fun onCardSelected(card: CardVO) {
        val simpleState = _gameStateFlow.value as? GameStateVO.Started ?: return
        websocketService.sendCommand(
            WebsocketRequest.SendAction(
                gameSessionId, currentUserRepository.currentUserId.orEmpty(),
                Action.turn,
                SimpleGameStateDTO(simpleState.gameNum, simpleState.circleNum),
                turn = CardDTO(card.suit, card.magnitude)
            )
        )
    }

    override fun onGameClosed() {
        websocketService.close()
    }

    override fun onPauseRequest() {
        websocketService.sendCommand(
            WebsocketRequest.SendAction(
                gameSessionId, currentUserRepository.currentUserId.orEmpty(),
                Action.pause,
            )
        )
    }

    override fun onResumeRequest() {
        websocketService.sendCommand(
            WebsocketRequest.SendAction(
                gameSessionId, currentUserRepository.currentUserId.orEmpty(),
                Action.resume,
            )
        )
    }

    override fun onRetryRequest() {
        websocketService.sendCommand(
            WebsocketRequest.SendAction(
                gameSessionId, currentUserRepository.currentUserId.orEmpty(),
                Action.reconnect,
            )
        )
    }
}