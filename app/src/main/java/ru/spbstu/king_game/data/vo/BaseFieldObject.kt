package ru.spbstu.king_game.data.vo

import ru.spbstu.king_game.view.custom.FieldView

data class BaseFieldObject(
    val id: String,
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
) {
    val left get() = x
    val right get() = x + width
    val top get() = y
    val bottom get() = y + height

    constructor(obj: BaseFieldObject) : this(obj.id, obj.x, obj.y, obj.width, obj.height)

    fun clone(obj: BaseFieldObject) {
        x = obj.x
        y = obj.y
        width = obj.width
        height = obj.height
    }

    companion object {
        fun createEmpty(id: String): BaseFieldObject {
            return BaseFieldObject(id, 100, 100, FieldView.cardSize.width, FieldView.cardSize.height)
        }
    }
}