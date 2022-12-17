package ru.spbstu.king_game.data.vo

import ru.spbstu.king_game.data.dto.player.PlayerDTO
import ru.spbstu.king_game.data.dto.player.PlayerId

data class PlayerVO(
    val id: PlayerId,
    val name: String,
    val points: Int,
) {
    companion object {
        fun mapFrom(dto: PlayerDTO): PlayerVO = dto.let {
            return PlayerVO(
                id = it.id,
                name = it.name,
                points = it.points
            )
        }
    }
}