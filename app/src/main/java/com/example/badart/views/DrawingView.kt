package com.example.badart.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var drawPath: Path = Path()
    private var drawPaint: Paint = Paint()
    private var canvasPaint: Paint = Paint(Paint.DITHER_FLAG)
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var paintColor = Color.BLACK
    private var isEraser = false
    private var isFillMode = false
    private var isProcessing = false

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        drawPaint.color = paintColor
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 10f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(newBitmap)
            drawCanvas?.drawColor(Color.WHITE)

            canvasBitmap?.let {
                drawCanvas?.drawBitmap(it, 0f, 0f, canvasPaint)
            }
            canvasBitmap = newBitmap
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, canvasPaint)
        }
        if (!isFillMode && !isProcessing) {
            canvas.drawPath(drawPath, drawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isProcessing) return true

        val touchX = event.x
        val touchY = event.y

        if (isFillMode) {
            if (event.action == MotionEvent.ACTION_UP) {
                performFloodFill(touchX.toInt(), touchY.toInt(), paintColor)
            }
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setColor(newColor: Int) {
        paintColor = newColor
        drawPaint.color = paintColor
        isEraser = false
        isFillMode = false
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 10f
    }

    fun setEraser(active: Boolean) {
        isEraser = active
        isFillMode = false
        if (active) {
            drawPaint.color = Color.WHITE
            drawPaint.strokeWidth = 30f
        } else {
            drawPaint.color = paintColor
            drawPaint.strokeWidth = 10f
        }
    }

    fun setFillMode(active: Boolean) {
        isFillMode = active
        isEraser = false
    }

    fun clearCanvas() {
        if (isProcessing) return
        drawCanvas?.drawColor(Color.WHITE)
        invalidate()
    }

    fun getBitmap(): Bitmap? {
        return canvasBitmap
    }

    private fun performFloodFill(x: Int, y: Int, targetColor: Int) {
        val bitmap = canvasBitmap ?: return
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return

        isProcessing = true

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val width = bitmap.width
                val height = bitmap.height

                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                val startPixelIndex = y * width + x
                val startColor = pixels[startPixelIndex]

                if (startColor == targetColor) {
                    isProcessing = false
                    return@launch
                }

                val queue: Queue<Int> = LinkedList()
                queue.add(startPixelIndex)
                pixels[startPixelIndex] = targetColor

                while (!queue.isEmpty()) {
                    val index = queue.remove()
                    val cx = index % width
                    val cy = index / width

                    // Check 4 neighbors
                    // Left
                    if (cx > 0) {
                        val left = index - 1
                        if (pixels[left] == startColor) {
                            pixels[left] = targetColor
                            queue.add(left)
                        }
                    }
                    // Right
                    if (cx < width - 1) {
                        val right = index + 1
                        if (pixels[right] == startColor) {
                            pixels[right] = targetColor
                            queue.add(right)
                        }
                    }
                    // Up
                    if (cy > 0) {
                        val up = index - width
                        if (pixels[up] == startColor) {
                            pixels[up] = targetColor
                            queue.add(up)
                        }
                    }
                    // Down
                    if (cy < height - 1) {
                        val down = index + width
                        if (pixels[down] == startColor) {
                            pixels[down] = targetColor
                            queue.add(down)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                    invalidate()
                    isProcessing = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                isProcessing = false
            }
        }
    }
}