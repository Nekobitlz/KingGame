package ru.spbstu.king_game.data.vo

open class BaseFieldVO(
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
) {
    val left get() = x
    val right get() = x + width
    val top get() = y
    val bottom get() = y + height
}