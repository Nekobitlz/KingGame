package ru.spbstu.king_game.engine.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.spbstu.king_game.data.dto.card.CardSuit
import ru.spbstu.king_game.data.vo.BaseFieldObject
import ru.spbstu.king_game.data.vo.card.CardStatus
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.engine.repository.DependencyProvider

class DebugGameStateController : GameStateController {

    private val _gameStateFlow = MutableStateFlow<GameStateVO>(GameStateVO.NotInitialized)
    override val gameStateFlow get() = _gameStateFlow.asStateFlow()

    private val debugCards
        get() = mutableListOf<CardVO>().apply {
            repeat(8) {
                add(
                    CardVO(
                        CardSuit.values()[(0..3).random()],
                        (6..14).random(),
                        CardStatus.BACK,
                        BaseFieldObject.createEmpty("$it")
                    )
                )
            }
        }

    init {
        val players = mutableListOf<PlayerVO>().apply {
            repeat(4) {
                add(PlayerVO(it, "Player$it", 0))
            }
        }
        DependencyProvider.currentUserRepository.setCurrentUser(players.first())
        _gameStateFlow.tryEmit(
            GameStateVO.Started(
                startedBy = players.first().id,
                gameNum = 1,
                circleNum = 1,
                playerTurn = players.first().id,
                players = players,
                cards = debugCards,
                bribe = listOf()
            )
        )
    }

    override fun onCardSelected(card: CardVO) {
        val value = _gameStateFlow.value as? GameStateVO.Started ?: return
        CoroutineScope(Dispatchers.Main).launch {
            _gameStateFlow.emit(value.copy(bribe = mutableListOf<CardVO?>().apply {
                add(0, null)
                addAll(1, listOf(debugCards[0], debugCards[1], debugCards[2]))
            }))
        }
    }

    override fun onGameClosed() {
    }

    override fun onPauseRequest() {
    }

    override fun onResumeRequest() {
    }
}