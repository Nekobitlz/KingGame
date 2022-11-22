package ru.spbstu.king_game.view.custom

import ru.spbstu.king_game.data.vo.BaseFieldVO

data class DraggingState(
    val dragX: Float,
    val dragY: Float,
    val dragObject: BaseFieldVO,
    var isDragging: Boolean,
)