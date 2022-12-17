package ru.spbstu.king_game.data.dto.player

import kotlinx.serialization.SerialName

typealias PlayerId = String

@kotlinx.serialization.Serializable
data class PlayerDTO(
    @SerialName("player_id") val id: PlayerId,
    @SerialName("player_name") val name: String,
    val points: Int,
)