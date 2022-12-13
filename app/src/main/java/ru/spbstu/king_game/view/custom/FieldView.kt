package ru.spbstu.king_game.view.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.vo.*
import ru.spbstu.king_game.engine.data.FieldState
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.view.utils.dpToPx
import ru.spbstu.king_game.view.utils.spToPx
import java.lang.Integer.min

private const val CLICK_THRESHOLD: Int = 100

class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var draggingState: DraggingState? = null

    var fieldState: FieldState? = null
    var currentUserRepository: CurrentUserRepository? = null
    var onCardSelected: (CardVO) -> Unit = { card ->
        val enemies = fieldState?.players?.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        fieldState?.currentStep = enemies?.get(2)!!
        fieldState?.let { state -> updateState(state) }
    }

    private val closedCardDrawable = RotateDrawable().apply {
        drawable = ContextCompat.getDrawable(context, R.drawable.closed_card)
    }

    private val cardSize = Size(65.dpToPx.toInt(), 85.dpToPx.toInt())
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 13.spToPx
        isAntiAlias = true
    }
    private val titlePaint = Paint(textPaint).apply {
        textSize = 18.spToPx
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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


    override fun onDraw(canvas: Canvas) {
        val fieldState = fieldState ?: return
        val enemies = fieldState.players.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        drawFirstEnemy(canvas, enemies.getOrNull(0))
        drawSecondEnemy(canvas, enemies.getOrNull(1))
        drawThirdEnemy(canvas, enemies.getOrNull(2))
        drawCurrentPlayer(canvas)
        drawTitle(canvas, fieldState.currentMode)
        for (card in fieldState.deck) {
            val drawable = pickCardDrawable(card)
            val cardField = card.fieldState
            drawable.level = 0
            drawable.setBounds(
                cardField.left,
                cardField.top,
                cardField.right,
                cardField.bottom
            )
            drawable.draw(canvas)
        }
    }

    private fun drawTitle(canvas: Canvas, currentMode: String) {
        val measuredText = titlePaint.measureText(currentMode)
        canvas.drawText(currentMode, width / 2 - measuredText / 2f, 12.dpToPx, titlePaint)
    }

    fun updateState(fieldState: FieldState) {
        val enemies = fieldState.players.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        fieldState.deck.forEachIndexed { i, card ->
            val cardState = card.fieldState
            val id = enemies.getOrNull(i)?.id ?: 0
            playersCardRect[id]?.let {
                cardState.x = it.left.toInt()
                cardState.y = it.top.toInt()
            }
        }
        invalidate()
    }

    private fun drawCurrentPlayer(canvas: Canvas) {
        getCurrentPlayer()?.let {
            val topPadding = 2.dpToPx
            val startPoint = PointF(width / 2f - cardSize.width / 2f, height / 2f + topPadding)
            drawCardBorder(canvas, startPoint, it.id)

            val maxWidth = min(width - 16.dpToPx.toInt() * 2, cardSize.width * it.cards.size)
            val cardOffset = maxWidth / it.cards.size
            val startOffset = (width - cardOffset * it.cards.size) / 2
            if (fieldState?.currentStep == getCurrentPlayer()) {
                canvas.drawText(
                    yourStepText,
                    width / 2f - measuredTextSize,
                    height - cardSize.height - 14.dpToPx,
                    textPaint
                )
            }
            it.cards.forEachIndexed { i, card ->
                val offset = startOffset + i * cardOffset
                val cardField = card.fieldState
                if (draggingState == null || draggingState?.dragObject?.id != cardField.id) {
                    cardField.x = offset
                    cardField.y = height - cardField.height
                }
                val openedCardDrawable = pickCardDrawable(card)
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
            height / 2f - cardSize.height - topPadding
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxWidth = min(width / 2, cardSize.width * player.cards.size)
        val cardOffset = maxWidth / player.cards.size
        val startOffset = (width - cardOffset * player.cards.size) / 2
        player.cards.forEachIndexed { i, card ->
            val offset = startOffset + cardOffset * i
            val drawable = if (card.cardStatus == CardStatus.BACK) closedCardDrawable else pickCardDrawable(card)
            val cardField = card.fieldState
            cardField.x = offset
            cardField.y = (titlePaint.textSize + 12.dpToPx).toInt()
            drawable.level = 0
            drawable.setBounds(
                cardField.left,
                cardField.top,
                cardField.right,
                cardField.bottom
            )
            drawable.draw(canvas)
            if (i == 0) {
                val text = if (fieldState?.currentStep == player) {
                    player.name + "- сейчас ходит"
                } else player.name
                canvas.drawText(
                    text,
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
            val drawable = if (card.cardStatus == CardStatus.BACK) closedCardDrawable else pickCardDrawable(card)
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
                val text = if (fieldState?.currentStep == player) {
                    player.name + "- сейчас ходит"
                } else player.name
                canvas.drawText(
                    text,
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
            val drawable = if (card.cardStatus == CardStatus.BACK) closedCardDrawable else pickCardDrawable(card)
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
                val text = if (fieldState?.currentStep == player) {
                    "сейчас ходит - ${player.name}"
                } else player.name
                canvas.drawText(
                    text,
                    cardField.x.toFloat() - textPaint.measureText(text),
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
                val card = getTouchedCard(evX, evY)
                if (card != null) {
                    val cardState = card.fieldState
                    draggingState = DraggingState(
                        evX - cardState.x, evY - cardState.y,
                        cardState, true
                    )
                    draggingState?.let {
                        if (it.isDragging) {
                            it.dragObject.x = (evX - it.dragX).toInt()
                            it.dragObject.y = (evY - it.dragY).toInt()
                            invalidate()
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
                if (event.eventTime - event.downTime < CLICK_THRESHOLD) {
                    return onCardClick(event)
                }
                val leftPadding = cardSize.width / 2
                val topPadding = cardSize.height / 2
                val rect = RectF(playersCardRect[player.id]).apply {
                    set(
                        left - leftPadding, top - topPadding,
                        right + leftPadding, bottom + topPadding
                    )
                }
                if (cardInRect(it.dragObject, rect)) {
                    it.dragObject.x = (rect.left + leftPadding).toInt()
                    it.dragObject.y = (rect.top + topPadding).toInt()
                    getTouchedCard(evX, evY)?.let { card ->
                        onCardSelected.invoke(card)
                    }
                } else {
                    draggingState?.reset()
                    draggingState = null
                }
                invalidate()
            }
        }
        return true
    }

    private fun cardInRect(
        dragObject: BaseFieldObject?,
        rect: RectF?
    ) = dragObject != null && rect != null
            && dragObject.x.toFloat() in rect.left..rect.right
            && dragObject.y.toFloat() in rect.top..rect.bottom

    private fun getTouchedCard(evX: Float, evY: Float): CardVO? {
        val currentPlayer = getCurrentPlayer() ?: return null
        for (card in currentPlayer.cards) {
            val cardState = card.fieldState
            if (evX >= cardState.left && evX <= cardState.right
                && evY >= cardState.top && evY <= cardState.bottom
            ) {
                return card
            }
        }
        return null
    }

    private fun onCardClick(e: MotionEvent): Boolean {
        val currentPlayer = getCurrentPlayer() ?: return false
        val card = getTouchedCard(e.x, e.y) ?: return false
        animateCardToDeck(card.fieldState, currentPlayer)
        onCardSelected.invoke(card)
        return true
    }

    private fun pickCardDrawable(card: CardVO): Drawable {
        var res = CardMapper.mapFrom(card)
        if (res <= 0) {
            res = R.drawable.closed_card
        }
        return ContextCompat.getDrawable(context, res)!!
    }

    private fun animateCardToDeck(cardState: BaseFieldObject, player: PlayerVO) {
        val rect = playersCardRect[player.id] ?: return

        val pvhX = PropertyValuesHolder.ofInt("x", cardState.x, rect.left.toInt())
        val pvhY = PropertyValuesHolder.ofInt("y", cardState.y, rect.top.toInt())

        val translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY)
        translator.addUpdateListener { valueAnimator ->
            cardState.x = valueAnimator.getAnimatedValue("x") as Int
            cardState.y = valueAnimator.getAnimatedValue("y") as Int
            invalidate()
        }
        translator.duration = 300
        translator.start()
    }
}
