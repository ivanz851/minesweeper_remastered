package com.ivanz851.minesweeper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.ivanz851.minesweeper.listeners.OnGameEndListener
import com.ivanz851.minesweeper.listeners.OnHintsCountChangeListener
import com.ivanz851.minesweeper.listeners.OnScoreChangeListener
import kotlin.math.min
import kotlin.random.Random


class GameFieldView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var boardWidth = 6
    private var boardHeight = 11
    private var mineCount = (boardWidth * boardHeight * 0.12).toInt()
    private var cellSize = 100

    private var boardPixelWidth = 0
    private var boardPixelHeight = 0

    private var horizontalOffset = 0f
    private var verticalOffset = 0f

    private lateinit var cells: Array<Array<Cell>>
    private var bitmapMine: Bitmap? = null
    private var bitmapFlag: Bitmap? = null

    private var score = 0

    private var hintSwitch: SwitchCompat? = null
    private var hintsCount: Int = 0

    private var firstTurn: Boolean = true


    private val paintLine = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }


    private fun paintText(textColor: Int): Paint {
        return Paint().apply {
            color = textColor
            textSize = 48f
            strokeWidth = 5f
            style = Paint.Style.FILL_AND_STROKE
            textAlign = Paint.Align.CENTER
        }
    }


    private val paintCell = Paint().apply {
        color = Color.argb(0xFF, 0x99, 0x99, 0x99)
        style = Paint.Style.FILL
    }


    init {
        isFocusable = true
        isFocusableInTouchMode = true
        generateBoard(boardWidth, boardHeight)
    }


    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val x = ((e.x - horizontalOffset) / cellSize).toInt()
            val y = ((e.y - verticalOffset) / cellSize).toInt()

            if (x in 0 until boardWidth && y in 0 until boardHeight) {
                if (firstTurn) {
                    while (cells[x][y].isMine) {
                        generateBoard(boardWidth, boardHeight)
                    }
                    firstTurn = false
                }

                val cell = cells[x][y]
                if (!cell.isRevealed && !cells[x][y].isFlagged) {
                    revealCell(x, y)
                    checkWinCondition()
                }
            }
            return true
        }


        override fun onLongPress(e: MotionEvent) {
            val x = ((e.x - horizontalOffset) / cellSize).toInt()
            val y = ((e.y - verticalOffset) / cellSize).toInt()

            if (x in 0 until boardWidth && y in 0 until boardHeight) {
                val cell = cells[x][y]
                if (!cell.isRevealed) {
                    cell.isFlagged = !cell.isFlagged
                    invalidate()
                }
            }
        }
    })


    public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val screenWidth = width
        val screenHeight = height
        cellSize = min(screenWidth / boardWidth, screenHeight / boardHeight)

        boardPixelWidth = boardWidth * cellSize
        boardPixelHeight = boardHeight * cellSize

        horizontalOffset = (width - boardPixelWidth) / 2f
        verticalOffset = (height - boardPixelHeight) / 2f

        bitmapMine = BitmapFactory.decodeResource(resources, R.drawable.mine)
        bitmapMine = Bitmap.createScaledBitmap(bitmapMine!!, cellSize, cellSize, false)

        bitmapFlag = BitmapFactory.decodeResource(resources, R.drawable.flag)
        bitmapFlag = Bitmap.createScaledBitmap(bitmapFlag!!, cellSize, cellSize, false)
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {

            verticalOffset = (height - boardPixelHeight) / 2f
            horizontalOffset = (width - boardPixelWidth) / 2f

            for (x in 0 until boardWidth) {
                for (y in 0 until boardHeight) {
                    val cell = cells[x][y]
                    if (cell.isRevealed) {
                        if (cell.isMine) {
                            drawBitmap(
                                bitmapMine!!,
                                null,
                                Rect(
                                    (horizontalOffset + x * cellSize).toInt(),
                                    (verticalOffset + y * cellSize).toInt(),
                                    (horizontalOffset + (x + 1) * cellSize).toInt(),
                                    (verticalOffset + (y + 1) * cellSize).toInt()
                                ),
                                null
                            )
                        } else {
                            drawRect(
                                horizontalOffset + (x * cellSize),
                                verticalOffset + (y * cellSize),
                                horizontalOffset + ((x + 1) * cellSize),
                                verticalOffset + ((y + 1) * cellSize),
                                paintCell
                            )

                            if (cell.mineCount > 0) {
                                val paint = when (cell.mineCount) {
                                    1 -> paintText(Color.argb(0xFF, 0x06, 0x0b, 0x9e))
                                    2 -> paintText(Color.argb(0xFF, 0x30, 0x85, 0x05))
                                    3 -> paintText(Color.argb(0xFF, 0xad, 0x03, 0x03))
                                    else -> paintText(Color.BLACK)
                                }
                                drawText(
                                    cell.mineCount.toString(),
                                    horizontalOffset + (x * cellSize) + (cellSize / 2),
                                    verticalOffset + (y * cellSize) + (cellSize / 2) - (paint.ascent() + paint.descent()) / 2,
                                    paint
                                )
                            }
                        }
                    } else if (cell.isFlagged) {
                        drawBitmap(
                            bitmapFlag!!,
                            null,
                            Rect(
                                (horizontalOffset + x * cellSize).toInt(),
                                (verticalOffset + y * cellSize).toInt(),
                                (horizontalOffset + (x + 1) * cellSize).toInt(),
                                (verticalOffset + (y + 1) * cellSize).toInt()
                            ),
                            null
                        )
                    }
                    drawRect(
                        horizontalOffset + (x * cellSize),
                        verticalOffset + (y * cellSize),
                        horizontalOffset + ((x + 1) * cellSize),
                        verticalOffset + ((y + 1) * cellSize),
                        paintLine
                    )
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }
        return true
    }


    override fun performClick(): Boolean {
        super.performClick()
        return true
    }


    private fun revealCell(x: Int, y: Int) {
        val cell = cells[x][y]
        if (cell.isRevealed) {
            return
        }

        if (hintSwitch?.isChecked == false && cell.isMine) {
            cells.forEach { row ->
                row.forEach { c ->
                    c.isRevealed = true
                }
            }
            gameEndListener?.onGameEnd(false)
        } else {
            cell.isRevealed = true
            score++
            scoreChangeListener?.onScoreChanged(score)

            if (cell.mineCount == 0) {
                for (xx in x - 1..x + 1) {
                    for (yy in y - 1..y + 1) {
                        if (xx in 0 until boardWidth && yy in 0 until boardHeight && !cells[xx][yy].isRevealed) {
                            revealCell(xx, yy)
                        }
                    }
                }
            }

            if (hintSwitch?.isChecked == true) {
                hintSwitch?.isChecked = false
                --hintsCount
                hintsCountChangeListener?.onHintCountChanged(hintsCount)
            }
        }
        invalidate()
    }



    private fun checkWinCondition() {
        var revealedCount = 0
        cells.forEach { row ->
            row.forEach { c ->
                if (c.isRevealed) {
                    revealedCount++
                }
            }
        }
        if (revealedCount == boardWidth * boardHeight - mineCount) {
            cells.forEach { row ->
                row.forEach { c ->
                    c.isRevealed = true
                }
            }
            gameEndListener?.onGameEnd(true)
            invalidate()
        }
    }


    private fun generateBoard(boardWidth: Int, boardHeight: Int) {
        firstTurn = true
        cells = Array(boardWidth) { x ->
            Array(boardHeight) { y ->
                Cell(x, y, isMine = false, isRevealed = false, mineCount = 0)
            }
        }
        repeat(mineCount) {
            var x: Int
            var y: Int
            do {
                x = Random.nextInt(boardWidth)
                y = Random.nextInt(boardHeight)
            } while (cells[x][y].isMine)
            cells[x][y].isMine = true
            for (xx in x - 1..x + 1) {
                for (yy in y - 1..y + 1) {
                    if (xx in 0 until boardWidth && yy in 0 until boardHeight) {
                        cells[xx][yy].mineCount++
                    }
                }
            }
        }

    }


    fun resetGame() {
        generateBoard(boardWidth, boardHeight)
        score = 0
        firstTurn = true
        scoreChangeListener?.onScoreChanged(score)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapMine?.recycle()
        bitmapFlag?.recycle()
    }


    data class Cell(
        val x: Int,
        val y: Int,
        var isMine: Boolean,
        var isRevealed: Boolean,
        var mineCount: Int,
        var isFlagged: Boolean = false
    )


    private var scoreChangeListener: OnScoreChangeListener? = null
    fun setOnScoreChangeListener(listener: OnScoreChangeListener) {
        scoreChangeListener = listener
    }


    private var hintsCountChangeListener: OnHintsCountChangeListener? = null
    fun setOnHintsCountChangeListener(listener: OnHintsCountChangeListener) {
        hintsCountChangeListener = listener
    }


    fun setOnGameEndListener(listener: OnGameEndListener) {
        gameEndListener = listener
    }


    fun getCurrentScore(): Int {
        return score
    }


    fun setBoardSize(width: Int, height: Int) {
        boardWidth = width
        boardHeight = height
        mineCount = (boardWidth * boardHeight * 0.12).toInt()
    }


    fun setHintSwitch(hintSwitch: SwitchCompat) {
        this.hintSwitch = hintSwitch
    }

    private var gameEndListener: OnGameEndListener? = null
}