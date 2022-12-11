package ru.spbstu.king_game.view.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.view.utils.spToPx
import ru.spbstu.king_game.view.utils.dpToPx
import java.lang.Integer.min


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

    private val cardSize = Size(65.dpToPx.toInt(), 85.dpToPx.toInt())
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 13.spToPx
        isAntiAlias = true
    }
    private val borderCardPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        alpha = 255 / 4
        isAntiAlias = true
    }
    private val yourStepText = resources.getString(R.string.your_step)
    private val measuredTextSize = textPaint.measureText(yourStepText) / 2

    private val playersCardRect = mutableMapOf<String, RectF>()

    init {
        setBackgroundResource(R.drawable.rectangle_background)
    }

    override fun onDraw(canvas: Canvas) {
        val fieldState = fieldState ?: return
        val enemies = fieldState.players.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        drawFirstEnemy(canvas, enemies.getOrNull(0))
        drawSecondEnemy(canvas, enemies.getOrNull(1))
        drawThirdEnemy(canvas, enemies.getOrNull(2))
        drawCurrentPlayer(canvas)

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
            val topPadding = 2.dpToPx
            val startPoint = PointF(width / 2f - cardSize.width / 2f, height / 2f + topPadding)
            drawCardBorder(canvas, startPoint, it.id)

            val maxWidth = min(width - 16.dpToPx.toInt() * 2, cardSize.width * it.cards.size)
            val cardOffset = maxWidth / it.cards.size
            val startOffset = (width - cardOffset * it.cards.size) / 2
            canvas.drawText(
                yourStepText,
                width / 2f - measuredTextSize,
                height - cardSize.height - 14.dpToPx,
                textPaint
            )
            it.cards.forEachIndexed { i, card ->
                val offset = startOffset + i * cardOffset
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
            }
        }
    }

    private fun drawFirstEnemy(canvas: Canvas, player: PlayerVO?) {
        player ?: return
        val topPadding = 2.dpToPx
        val startPoint = PointF(
            width / 2f - cardSize.width / 2f,
            height / 2f - cardSize.height - topPadding)
        drawCardBorder(canvas, startPoint, player.id)

        val maxWidth = min(width / 2, cardSize.width * player.cards.size)
        val cardOffset = maxWidth / player.cards.size
        val startOffset = (width - cardOffset * player.cards.size) / 2
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

        val rightPadding = 4.dpToPx
        val startPoint = PointF(
            width / 2f - cardSize.width * 1.5f - rightPadding,
            height / 2f - cardSize.height / 2f
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxHeight = min(height / 3, cardSize.height * player.cards.size)
        val cardOffset = maxHeight / player.cards.size
        val startOffset = (height - cardOffset * player.cards.size) / 2
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

        val leftPadding = 4.dpToPx
        val startPoint = PointF(
            width / 2f + cardSize.width / 2f + leftPadding,
            height / 2f - cardSize.height / 2f
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxHeight = min(height / 3, cardSize.height * player.cards.size)
        val cardOffset = maxHeight / player.cards.size
        val startOffset = (height - cardOffset * player.cards.size) / 2
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

    private fun drawCardBorder(canvas: Canvas, startPoint: PointF, id: String) {
        val roundedRadius = 4.dpToPx
        val rectF = RectF(
            startPoint.x, startPoint.y,
            startPoint.x + cardSize.width.toFloat(),
            startPoint.y + cardSize.height.toFloat()
        )
        playersCardRect[id] = rectF
        canvas.drawRoundRect(rectF, roundedRadius, roundedRadius, borderCardPaint)
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
            MotionEvent.ACTION_UP -> draggingState?.let {
                it.isDragging = false
                val player = getCurrentPlayer() ?: return false
                val leftPadding = cardSize.width / 2
                val topPadding = cardSize.height / 2
                val rect = playersCardRect[player.id]?.apply {
                    set(left - leftPadding, top - topPadding,
                        right + leftPadding, bottom + topPadding)
                }
                if (rect != null
                    && it.dragObject.x.toFloat() in rect.left..rect.right
                    && it.dragObject.y.toFloat() in rect.top..rect.bottom
                ) {
                    it.dragObject.x = (rect.left + leftPadding).toInt()
                    it.dragObject.y = (rect.top + topPadding).toInt()
                } else {
                    draggingState?.reset()
                    draggingState = null
                }
                invalidate()
            }
        }
        return true
    }
}
