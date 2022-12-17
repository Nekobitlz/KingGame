package ru.spbstu.king_game.data.vo.game

import ru.spbstu.king_game.data.dto.game.GameStateDTO
import ru.spbstu.king_game.data.dto.player.PlayerId
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.engine.data.State

sealed class GameStateVO {

    data class Started(
        val startedBy: PlayerId,
        val gameNum: Int,
        val circleNum: Int,
        val playerTurn: PlayerId,
        val players: List<PlayerVO>,
        val cards: List<CardVO>,
        val bribe: List<CardVO?>
    ) : GameStateVO()

    data class Paused(
        val pausedBy: PlayerId,
        val gameNum: Int,
        val circleNum: Int,
        val players: List<PlayerVO>,
    ) : GameStateVO()

    data class Cancelled(
        val cancelledBy: PlayerId,
        val gameNum: Int,
        val circleNum: Int,
        val players: List<PlayerVO>,
    ) : GameStateVO()

    data class Finished(
        val winner: PlayerId,
        val players: List<PlayerVO>,
    ) : GameStateVO()

    object NotInitialized: GameStateVO()

    companion object {
        fun mapFrom(state: GameStateDTO): GameStateVO = state.let { dto ->
            return when (dto.state) {
                State.started -> Started(
                    startedBy = dto.startedBy.orEmpty(),
                    gameNum = dto.gameNum ?: 0,
                    circleNum = dto.circleNum ?: 0,
                    playerTurn = dto.playerTurn ?: dto.players?.firstOrNull()?.id.orEmpty(),
                    players = dto.players?.map { PlayerVO.mapFrom(it) }.orEmpty(),
                    cards = dto.cards?.map { CardVO.mapFrom(it) }.orEmpty(),
                    bribe = dto.bribe?.map { CardVO.mapFrom(it) }.orEmpty(),
                )
                State.finished -> Finished(
                    winner = dto.winner.orEmpty(),
                    players = dto.players?.map { PlayerVO.mapFrom(it) }.orEmpty(),
                )
                State.paused -> Paused(
                    pausedBy = dto.pausedBy.orEmpty(),
                    gameNum = dto.gameNum ?: 0,
                    circleNum = dto.circleNum ?: 0,
                    players = dto.players?.map { PlayerVO.mapFrom(it) }.orEmpty(),
                )
                State.cancelled -> Cancelled(
                    cancelledBy = dto.cancelledBy.orEmpty(),
                    gameNum = dto.gameNum ?: 0,
                    circleNum = dto.circleNum ?: 0,
                    players = dto.players?.map { PlayerVO.mapFrom(it) }.orEmpty(),
                )
            }
        }
    }
}