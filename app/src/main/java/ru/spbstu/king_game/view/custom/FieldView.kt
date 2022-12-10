package ru.spbstu.king_game.view.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.view.utils.spToPx
import ru.spbstu.king_game.view.utils.dpToPx


class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var draggingState: DraggingState? = null

    var fieldState: FieldState? = null
    var currentUserRepository: CurrentUserRepository? = null

    private val closedCardDrawable = RotateDrawable().apply {
        drawable = ContextCompat.getDrawable(context, R.drawable.closed_card_image)
    }
    private val openedCardDrawable = ContextCompat.getDrawable(context, R.drawable.valet_card)!!

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 13.spToPx
        isAntiAlias = true
    }

    private val yourStepText = resources.getString(R.string.your_step)

    init {
        setBackgroundResource(R.drawable.table_background)
    }

    override fun onDraw(canvas: Canvas) {
        val fieldState = fieldState ?: return
        val enemies = fieldState.players.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        drawCurrentPlayer(canvas)
        drawFirstEnemy(canvas, enemies.getOrNull(0))
        drawSecondEnemy(canvas, enemies.getOrNull(1))
        drawThirdEnemy(canvas, enemies.getOrNull(2))

        for (card in fieldState.deck) {
            val cardState = card.fieldState
            openedCardDrawable.setBounds(
                cardState.left,
                cardState.top,
                cardState.right,
                cardState.bottom
            )
            openedCardDrawable.draw(canvas)
        }
    }

    private fun drawCurrentPlayer(canvas: Canvas) {
        getCurrentPlayer()?.let {
            val maxWidth = (width - 16.dpToPx.toInt() * 2)
            val startOffset = 8.dpToPx.toInt()
            val bottomOffset = maxWidth / it.cards.size
            it.cards.forEachIndexed { i, card ->
                val offset = startOffset + i * bottomOffset
                val cardField = card.fieldState
                if (draggingState == null || draggingState?.dragObject?.id != cardField.id) {
                    cardField.x = offset
                    cardField.y = height - cardField.height
                }
                openedCardDrawable.setBounds(
                    cardField.left,
                    cardField.top,
                    cardField.right,
                    cardField.bottom
                )
                openedCardDrawable.draw(canvas)
                if (i == it.cards.lastIndex) {
                    canvas.drawText(
                        yourStepText,
                        width / 2f,
                        cardField.top.toFloat() - 14.dpToPx,
                        textPaint
                    )
                }
            }
        }
    }

    private fun drawFirstEnemy(canvas: Canvas, player: PlayerVO?) {
        player ?: return
        val startOffset = width / 5
        val maxWidth = width / 2
        val cardOffset = maxWidth / player.cards.size
        player.cards.forEachIndexed { i, card ->
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
            val cardField = card.fieldState
            cardField.x = offset
            drawable.level = 0
            drawable.setBounds(
                cardField.left,
                cardField.top,
                cardField.right,
                cardField.bottom
            )
            drawable.draw(canvas)
            if (i == 0) {
                canvas.drawText(
                    player.name,
                    cardField.left.toFloat() + 14.dpToPx,
                    cardField.bottom.toFloat() + 14.dpToPx, textPaint
                )
            }
        }
    }

    private fun drawSecondEnemy(canvas: Canvas, player: PlayerVO?) {
        player ?: return
        val maxHeight = height / 3
        val cardOffset = maxHeight / player.cards.size
        val startOffset = height / 5 * 2
        player.cards.forEachIndexed { i, card ->
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
            val cardField = card.fieldState
            cardField.x = -cardField.width / 2
            cardField.y = height - offset
            drawable.level = 2500
            drawable.setBounds(
                cardField.left,
                cardField.top,
                cardField.right,
                cardField.bottom
            )
            drawable.draw(canvas)
            if (i == player.cards.lastIndex) {
                canvas.drawText(
                    player.name,
                    cardField.x + cardField.width / 2f + 14.dpToPx,
                    cardField.top.toFloat(), textPaint
                )
            }
        }
    }

    private fun drawThirdEnemy(canvas: Canvas, player: PlayerVO?) {
        player ?: return
        val maxHeight = height / 3
        val cardOffset = maxHeight / player.cards.size
        val startOffset = height / 5 * 2
        player.cards.forEachIndexed { i, card ->
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
            val cardField = card.fieldState
            cardField.x = width - cardField.width / 2
            cardField.y = height - offset
            drawable.level = 7500
            drawable.setBounds(
                cardField.left,
                cardField.top,
                cardField.right,
                cardField.bottom
            )
            drawable.draw(canvas)
            if (i == player.cards.lastIndex) {
                canvas.drawText(
                    player.name,
                    cardField.x.toFloat() - 14.dpToPx,
                    cardField.top.toFloat(), textPaint
                )
            }
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
                for (card in currentPlayer.cards) {
                    val cardState = card.fieldState
                    if (evX >= cardState.left && evX <= cardState.right
                        && evY >= cardState.top && evY <= cardState.bottom
                    ) {
                        // Activate the drag mode
                        draggingState = DraggingState(
                            evX - cardState.x, evY - cardState.y,
                            cardState, true
                        )
                        draggingState?.let {
                            if (it.isDragging) {
                                // Defines new coordinates for drawing
                                it.dragObject.x = (evX - it.dragX).toInt()
                                it.dragObject.y = (evY - it.dragY).toInt()
                                invalidate()
                            }
                        }
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
            MotionEvent.ACTION_UP -> {
                draggingState?.isDragging = false
                draggingState?.reset()
                draggingState = null
                invalidate()
            }
        }
        return true
    }
}
