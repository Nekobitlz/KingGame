package ru.spbstu.king_game.data.dto.game

import kotlinx.serialization.SerialName
import ru.spbstu.king_game.data.dto.card.CardDTO
import ru.spbstu.king_game.data.dto.player.PlayerDTO
import ru.spbstu.king_game.data.dto.player.PlayerId
import ru.spbstu.king_game.engine.data.State

@kotlinx.serialization.Serializable
data class GameStateDTO(
    val state: State,
    @SerialName("started_by") val startedBy: PlayerId? = null,
    @SerialName("paused_by") val pausedBy: PlayerId? = null,
    @SerialName("cancelled_by") val cancelledBy: PlayerId? = null,
    @SerialName("winner") val winner: PlayerId? = null,
    @SerialName("game_num") val gameNum: Int? = null,
    @SerialName("circle_num") val circleNum: Int? = null,
    @SerialName("player_turn") val playerTurn: PlayerId? = null,
    val players: List<PlayerDTO>? = null,
    val cards: List<CardDTO>? = null,
    val bribe: List<CardDTO>? = null,
)