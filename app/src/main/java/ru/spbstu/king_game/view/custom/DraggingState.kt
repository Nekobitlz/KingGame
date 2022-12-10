package ru.spbstu.king_game.view.custom

import ru.spbstu.king_game.data.vo.BaseFieldObject

data class DraggingState(
    val dragX: Float,
    val dragY: Float,
    val dragObject: BaseFieldObject,
    var isDragging: Boolean,
) {
    private val initialObject = BaseFieldObject(dragObject)

    fun reset() {
        dragObject.clone(initialObject)
    }
}
