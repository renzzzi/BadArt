package com.example.badart.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorValueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private var hue = 0f
    private var saturation = 1f
    private var currentValue = 1f

    private var onValueChangedListener: ((Float) -> Unit)? = null

    init {
        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 5f
        selectorPaint.color = Color.WHITE

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f
        borderPaint.color = Color.GRAY
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        updateShader()
    }

    fun setHueSat(h: Float, s: Float) {
        hue = h
        saturation = s
        updateShader()
        invalidate()
    }

    private fun updateShader() {
        if (rect.isEmpty) return

        val topColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
        val bottomColor = Color.BLACK

        val gradient = LinearGradient(
            0f, 0f, 0f, rect.height(),
            topColor, bottomColor, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(rect, 12f, 12f, paint)
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

        val selectorY = rect.height() * (1f - currentValue)

        selectorPaint.color = if (currentValue < 0.5f) Color.WHITE else Color.BLACK
        selectorPaint.strokeWidth = 6f

        val halfW = width.toFloat()
        canvas.drawLine(0f, selectorY, width.toFloat(), selectorY, selectorPaint)

        selectorPaint.style = Paint.Style.FILL
        canvas.drawCircle(halfW, selectorY, 8f, selectorPaint)
        selectorPaint.style = Paint.Style.STROKE // Reset
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                var v = 1f - (y / height.toFloat())
                if (v < 0f) v = 0f
                if (v > 1f) v = 1f

                currentValue = v
                onValueChangedListener?.invoke(currentValue)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnValueChangedListener(listener: (Float) -> Unit) {
        onValueChangedListener = listener
    }

    fun getValue(): Float = currentValue
}