package ru.spbstu.king_game.engine.repository

import ru.spbstu.king_game.data.dto.player.PlayerId
import ru.spbstu.king_game.data.vo.PlayerVO

class CurrentUserRepository {

    val currentUserId: PlayerId? get() = currentUser?.id

    var currentName: String? = null

    private var currentUser: PlayerVO? = null

    fun setCurrentUser(user: PlayerVO) {
        this.currentUser = user
    }

    fun isCurrent(id: PlayerId) = currentUserId == id
}