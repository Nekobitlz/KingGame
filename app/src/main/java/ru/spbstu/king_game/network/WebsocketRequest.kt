package ru.spbstu.king_game.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.spbstu.king_game.data.dto.card.CardDTO
import ru.spbstu.king_game.engine.data.Action
import ru.spbstu.king_game.data.dto.game.GameStateDTO
import ru.spbstu.king_game.data.dto.game.SimpleGameStateDTO
import ru.spbstu.king_game.data.dto.player.PlayerId

sealed class WebsocketRequest {

    @Serializable
    data class StartGame(
        @SerialName("session_id") val sessionId: String,
        @SerialName("player_name") val playerName: String,
        val action: Action,
    ) : WebsocketRequest()

    @Serializable
    data class SendAction(
        @SerialName("game_session_id") val gameSessionId: Long,
        @SerialName("player_id") val playerId: PlayerId,
        val action: Action,
        @SerialName("game_state") val gameState: SimpleGameStateDTO? = null,
        val turn: CardDTO? = null,
    ) : WebsocketRequest()


}

sealed class WebsocketResponse {
    @Serializable
    data class OpenSession(@SerialName("session_id") val sessionId: String) : WebsocketResponse()

    @Serializable
    data class UpdateGameState(
        @SerialName("game_session_id") val gameSessionId: Long,
        @SerialName("game_state") val gameState: GameStateDTO
    ) : WebsocketResponse()

    data class Error(val errorType: ErrorType): WebsocketResponse()
}

enum class ErrorType(val text: String) {
    SERVER_ERROR("Возникла ошибка связи с сервером, попробуйте позже"),
    CONNECTION_ERROR("Потеряна связь с сервером, попробуйте еще раз")
}