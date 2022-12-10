package ru.spbstu.king_game.engine.repository

import ru.spbstu.king_game.data.dto.player.User

class CurrentUserRepository {

    val currentUserId get() = "0"//currentUser?.id

    private var currentUser: User? = null

    fun setCurrentUser(user: User) {
        this.currentUser = user
    }

    fun isCurrent(id: String) = currentUserId == id
}