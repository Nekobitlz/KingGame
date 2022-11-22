package ru.spbstu.king_game.data.dto.card

typealias Magnitude = Int

fun Magnitude.isValid() = this in 7..14