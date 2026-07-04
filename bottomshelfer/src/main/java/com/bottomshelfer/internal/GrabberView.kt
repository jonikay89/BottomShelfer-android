package com.bottomshelfer.internal

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

@SuppressLint("ViewConstructor")
internal class GrabberView(
    context: Context,
    private val pillWidth: Float,
    private val pillHeight: Float,
    private var cornerRadius: Float
) : View(context) {

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        updatePillColor()
    }

    fun updatePillColor() {
        val dark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        pillPaint.color = if (dark) 0x99FFFFFF.toInt() else 0x99000000.toInt()
    }

    fun updateSize(width: Float, height: Float, radius: Float) {
        cornerRadius = radius
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(pillWidth.toInt(), widthMeasureSpec),
            resolveSize(pillHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, pillPaint)
    }
}
