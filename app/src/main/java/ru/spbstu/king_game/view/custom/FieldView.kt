package ru.spbstu.king_game.view.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.spbstu.king_game.R
import ru.spbstu.king_game.data.dto.player.PlayerId
import ru.spbstu.king_game.data.vo.BaseFieldObject
import ru.spbstu.king_game.data.vo.PlayerVO
import ru.spbstu.king_game.data.vo.card.CardMapper
import ru.spbstu.king_game.data.vo.card.CardVO
import ru.spbstu.king_game.data.vo.game.GameStateVO
import ru.spbstu.king_game.engine.repository.CurrentUserRepository
import ru.spbstu.king_game.view.utils.dpToPx
import ru.spbstu.king_game.view.utils.spToPx
import java.lang.Integer.min

private const val CLICK_THRESHOLD: Int = 100
private const val MAX_CARDS_SIZE = 8

class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val cardSize = Size(65.dpToPx.toInt(), 85.dpToPx.toInt())
    }

    private var draggingState: DraggingState? = null

    var fieldState: GameStateVO? = null
    var currentUserRepository: CurrentUserRepository? = null
    var onCardSelected: (CardVO) -> Unit = {}

    private val closedCardDrawable = RotateDrawable().apply {
        drawable = ContextCompat.getDrawable(context, R.drawable.closed_card)
    }

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

    private var gameNum = -1
    private var circleNum = -1
    private var firstTurn = -1
    private val playersCardRect = mutableMapOf<PlayerId, RectF>()


    override fun onDraw(canvas: Canvas) {
        val fieldState = fieldState ?: return
        if (fieldState !is GameStateVO.Started) {
            return
        }
        val enemies = fieldState.players.filterNot {
            currentUserRepository?.isCurrent(it.id) ?: false
        }
        val cardsCount = MAX_CARDS_SIZE - fieldState.circleNum + 1
        drawFirstEnemy(canvas, enemies.getOrNull(1), cardsCount)
        drawSecondEnemy(canvas, enemies.getOrNull(0), cardsCount)
        drawThirdEnemy(canvas, enemies.getOrNull(2), cardsCount)
        drawCurrentPlayer(canvas, fieldState)
        drawTitle(canvas, "Не брать взятки") //todo map to title
        for (card in fieldState.bribe) {
            card ?: continue
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

    fun updateState(fieldState: GameStateVO.Started) {
        val players = fieldState.players
        if (firstTurn == -1 || fieldState.circleNum != circleNum || fieldState.gameNum != gameNum) {
            firstTurn = players.indexOfFirst { it.id == fieldState.playerTurn }
            circleNum = fieldState.circleNum
            gameNum = fieldState.gameNum
        }
        fieldState.bribe.forEachIndexed { i, card ->
            card?.fieldState?.let { cardState ->
                val id = players.getOrNull((firstTurn + i) % players.size)?.id
                Log.d("FIELD_DEBUG", "found=$id with index: firstTurn=${firstTurn}  i=$i rect: ${playersCardRect[id].toString()}")
                playersCardRect[id]?.let {
                    cardState.x = it.left.toInt()
                    cardState.y = it.top.toInt()
                }
            }
        }
    }

    private fun drawCurrentPlayer(canvas: Canvas, fieldState: GameStateVO.Started) {
        getCurrentPlayer()?.let {
            val topPadding = 2.dpToPx
            val startPoint = PointF(width / 2f - cardSize.width / 2f, height / 2f + topPadding)
            drawCardBorder(canvas, startPoint, it.id)

            val maxWidth = min(width - 16.dpToPx.toInt() * 2, cardSize.width * fieldState.cards.size)
            val cardOffset = maxWidth / fieldState.cards.size
            val startOffset = (width - cardOffset * fieldState.cards.size) / 2
            if (fieldState.playerTurn == getCurrentPlayer()?.id) {
                canvas.drawText(
                    yourStepText,
                    width / 2f - measuredTextSize,
                    height - cardSize.height - 14.dpToPx,
                    textPaint
                )
            }
            fieldState.cards.forEachIndexed { i, card ->
                val offset = startOffset + i * cardOffset
                var cardField = card.fieldState
                if (draggingState == null || draggingState?.dragObject?.id != cardField.id) {
                    cardField.x = offset
                    cardField.y = height - cardField.height
                } else {
                    cardField = draggingState!!.dragObject
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

    private fun drawFirstEnemy(canvas: Canvas, player: PlayerVO?, cardsCount: Int) {
        player ?: return
        val topPadding = 2.dpToPx
        val startPoint = PointF(
            width / 2f - cardSize.width / 2f,
            height / 2f - cardSize.height - topPadding
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxWidth = min(width / 2, cardSize.width * cardsCount)
        val cardOffset = maxWidth / cardsCount
        val startOffset = (width - cardOffset * cardsCount) / 2
        (0 until cardsCount).forEach { i ->
            val cardField = BaseFieldObject.createEmpty("${player.id}-i")
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
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
                val text = if (getCurrentGameState()?.playerTurn == player.id) {
                    player.name + " - сейчас ходит"
                } else player.name
                canvas.drawText(
                    text,
                    cardField.left.toFloat() + 14.dpToPx,
                    cardField.bottom.toFloat() + 14.dpToPx, textPaint
                )
            }
        }
    }

    private fun drawSecondEnemy(canvas: Canvas, player: PlayerVO?, cardsCount: Int) {
        player ?: return

        val rightPadding = 4.dpToPx
        val startPoint = PointF(
            width / 2f - cardSize.width * 1.5f - rightPadding,
            height / 2f - cardSize.height / 2f
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxHeight = min(height / 3, cardSize.height * cardsCount)
        val cardOffset = maxHeight / cardsCount
        val startOffset = (height - cardOffset * cardsCount) / 2
        (0 until cardsCount).forEach { i ->
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
            val cardField = BaseFieldObject.createEmpty("${player.id}-i")
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
            if (i == cardsCount - 1) {
                val text = if (getCurrentGameState()?.playerTurn == player.id) {
                    player.name + " - сейчас ходит"
                } else player.name
                canvas.drawText(
                    text,
                    cardField.x + cardField.width / 2f + 14.dpToPx,
                    cardField.top.toFloat(), textPaint
                )
            }
        }
    }

    private fun drawThirdEnemy(canvas: Canvas, player: PlayerVO?, cardsCount: Int) {
        player ?: return

        val leftPadding = 4.dpToPx
        val startPoint = PointF(
            width / 2f + cardSize.width / 2f + leftPadding,
            height / 2f - cardSize.height / 2f
        )
        drawCardBorder(canvas, startPoint, player.id)

        val maxHeight = min(height / 3, cardSize.height * cardsCount)
        val cardOffset = maxHeight / cardsCount
        val startOffset = (height - cardOffset * cardsCount) / 2
        (0 until cardsCount).forEach { i ->
            val offset = startOffset + cardOffset * i
            val drawable = closedCardDrawable
            val cardField = BaseFieldObject.createEmpty("${player.id}-i")
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
            if (i == cardsCount - 1) {
                val text = if (getCurrentGameState()?.playerTurn == player.id) {
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

    private fun drawCardBorder(canvas: Canvas, startPoint: PointF, id: PlayerId) {
        val roundedRadius = 4.dpToPx
        val rectF = RectF(
            startPoint.x, startPoint.y,
            startPoint.x + cardSize.width.toFloat(),
            startPoint.y + cardSize.height.toFloat()
        )
        playersCardRect[id] = rectF
        canvas.drawRoundRect(rectF, roundedRadius, roundedRadius, borderCardPaint)
    }

    private fun getCurrentPlayer() = (fieldState as? GameStateVO.Started)?.players?.find {
        currentUserRepository?.isCurrent(it.id) == true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val evX = event.x
        val evY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val card = getTouchedCard(evX, evY)
                val player = getCurrentPlayer() ?: return false
                if (card != null && getCurrentGameState()?.playerTurn == player.id) {
                    val cardState = card.fieldState
                    draggingState = DraggingState(
                        evX - cardState.x, evY - cardState.y,
                        cardState, true
                    )
                    draggingState?.let {
                        Log.d("FIELD_DEBUG", "ACTION_DOWN ${(evX - it.dragX).toInt()} ${(evY - it.dragY).toInt()} ${it.isDragging}")
                        if (it.isDragging) {
                            it.dragObject.x = (evX - it.dragX).toInt()
                            it.dragObject.y = (evY - it.dragY).toInt()
                            postInvalidate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> draggingState?.let {
                Log.d("FIELD_DEBUG", "ACTION_MOVE ${(evX - it.dragX).toInt()} ${(evY - it.dragY).toInt()} ${it.isDragging}")
                if (it.isDragging) {
                    it.dragObject.x = (evX - it.dragX).toInt()
                    it.dragObject.y = (evY - it.dragY).toInt()
                    postInvalidate()
                }
            }
            MotionEvent.ACTION_UP -> draggingState?.let {
                it.isDragging = false
                val player = getCurrentPlayer() ?: return false
                Log.d("FIELD_DEBUG", "ACTION_UP ${it.dragObject.x} ${it.dragObject.y}")
                if (event.eventTime - event.downTime < CLICK_THRESHOLD) {
                    Log.d("FIELD_DEBUG", "ACTION_UP click detected")
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
                postInvalidate()
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
        val fieldState = getCurrentGameState() ?: return null
        for (card in fieldState.cards) {
            val cardState = card.fieldState
            if (evX >= cardState.left && evX <= cardState.right
                && evY >= cardState.top && evY <= cardState.bottom
            ) {
                return card
            }
        }
        return null
    }

    private fun getCurrentGameState(): GameStateVO.Started? = fieldState as? GameStateVO.Started

    private fun onCardClick(e: MotionEvent): Boolean {
        val currentPlayer = getCurrentPlayer() ?: return false
        if (getCurrentGameState()?.playerTurn != currentPlayer.id) {
            return false
        }
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
        Log.d("FIELD_DEBUG", "animate ${player.id} rect: $rect")

        val pvhX = PropertyValuesHolder.ofInt("x", cardState.x, rect.left.toInt())
        val pvhY = PropertyValuesHolder.ofInt("y", cardState.y, rect.top.toInt())

        val translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY)
        translator.addUpdateListener { valueAnimator ->
            cardState.x = valueAnimator.getAnimatedValue("x") as Int
            cardState.y = valueAnimator.getAnimatedValue("y") as Int
            postInvalidate()
        }
        translator.duration = 300
        translator.start()
    }
}
