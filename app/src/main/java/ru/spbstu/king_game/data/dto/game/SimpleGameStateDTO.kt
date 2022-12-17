package ru.spbstu.king_game.data.dto.game

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SimpleGameStateDTO(
    @SerialName("game_num") val gameNum: Int,
    @SerialName("circle_num") val circleNum: Int,
)