package com.example.badart.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var selectedColor = Color.RED
    private var onColorSelectedListener: ((Int) -> Unit)? = null

    init {
        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 5f
        selectorPaint.color = Color.BLACK
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(centerX, centerY) - 20f
        updateShader()
    }

    private fun updateShader() {
        if (radius <= 0) return
        val hueShader = SweepGradient(centerX, centerY,
            intArrayOf(Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED),
            null
        )
        val saturationShader = RadialGradient(centerX, centerY, radius, Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP)
        paint.shader = ComposeShader(hueShader, saturationShader, PorterDuff.Mode.SRC_OVER)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, radius, paint)

        val hsv = FloatArray(3)
        Color.colorToHSV(selectedColor, hsv)
        val hue = hsv[0]
        val sat = hsv[1]

        val angle = Math.toRadians((-hue).toDouble())
        val dist = sat * radius

        val selX = centerX + dist * cos(angle).toFloat()
        val selY = centerY + dist * sin(angle).toFloat()

        selectorPaint.color = if (hsv[2] > 0.5f) Color.BLACK else Color.WHITE
        canvas.drawCircle(selX, selY, 15f, selectorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - centerX
        val y = event.y - centerY
        val dist = hypot(x, y)

        if (dist <= radius) {
            val angle = atan2(y, x)
            var unit = angle / (2 * Math.PI)
            if (unit < 0) unit += 1.0

            val hue = (360 - unit * 360).toFloat()
            val sat = (dist / radius)

            selectedColor = Color.HSVToColor(floatArrayOf(hue, sat, 1f))
            onColorSelectedListener?.invoke(selectedColor)
            invalidate()
        }
        return true
    }

    fun setColor(color: Int) {
        selectedColor = color
        invalidate()
    }

    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }
}