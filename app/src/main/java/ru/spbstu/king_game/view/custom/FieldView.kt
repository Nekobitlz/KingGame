package ru.spbstu.king_game.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.CardStatus
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.data.GameState
import ru.spbstu.king_game.engine.repository.CurrentUserRepository


class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var draggingState: DraggingState? = null

    var fieldState: FieldState? = null
    var currentUserRepository: CurrentUserRepository? = null

    val cardOffset = 40

    val cardSize = Size(200, 280)
    val closedCardDrawable = ContextCompat.getDrawable(context, R.drawable.closed_card_image)!!
    val openedCardDrawable = ContextCompat.getDrawable(context, R.drawable.valet_card)!!

    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
    }

    init {
        setBackgroundResource(R.drawable.table_background)
    }

    override fun onDraw(canvas: Canvas) {
        val fieldState = fieldState ?: return
        var angle = 0f
        for (player in fieldState.players.filterNot { it.id == currentUserRepository?.currentUserId }) {
            player.cards.forEachIndexed { i, cardVO ->
                val offset = cardOffset * i
                val drawable = if (cardVO.cardStatus == CardStatus.BACK) {
                    closedCardDrawable
                } else {
                    openedCardDrawable
                }
                drawable.setBounds(
                    cardVO.left + offset,
                    cardVO.top,
                    cardVO.right + offset,
                    cardVO.bottom
                )
                drawable.draw(canvas)
            }
            canvas.drawText(player.name, 15f, 550f, textPaint)
            canvas.save()
            angle += 90f
            canvas.rotate(angle, width / 2f, height / 2f)
        }

        getCurrentPlayer()?.let {
            val bottomOffset = (width - cardSize.width) / it.cards.size
            it.cards.forEachIndexed { i, cardVO ->
                val offset = i * bottomOffset
                openedCardDrawable.setBounds(
                    cardVO.left + offset,
                    cardVO.top,
                    cardVO.right + offset,
                    cardVO.bottom
                )
                openedCardDrawable.draw(canvas)
            }
            // todo text from resource
            canvas.drawText("Ваш ход", 480f, 1430f, textPaint)
        }

        for (card in fieldState.deck) {
            openedCardDrawable.setBounds(
                card.left,
                card.top,
                card.right,
                card.bottom
            )
            openedCardDrawable.draw(canvas)
        }
    }

    private fun getCurrentPlayer() = fieldState?.players?.find {
        currentUserRepository?.isCurrent(it.id) == true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val evX = event.x
        val evY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // If the touch is within the square
                val currentPlayer = getCurrentPlayer() ?: return false
                for (card in currentPlayer.cards)
                    if (evX >= card.left && evX <= card.right
                        && evY >= card.top && evY <= card.bottom
                    ) {
                        // Activate the drag mode
                        draggingState = DraggingState(
                            evX - card.x, evY - card.y,
                            card, true
                        )
                    }
                draggingState?.let {
                    if (it.isDragging) {
                        // Defines new coordinates for drawing
                        it.dragObject.x = (evX - it.dragX).toInt()
                        it.dragObject.y = (evY - it.dragY).toInt()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> draggingState?.let {
                if (it.isDragging) {
                    it.dragObject.x = (evX - it.dragX).toInt()
                    it.dragObject.y = (evY - it.dragY).toInt()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> draggingState?.isDragging = false
        }
        return true
    }
}
