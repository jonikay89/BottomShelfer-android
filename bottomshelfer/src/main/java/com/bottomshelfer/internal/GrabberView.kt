package com.bottomshelfer.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.min

@SuppressLint("ViewConstructor")
internal class GrabberView(
    context: Context,
    private val pillWidth: Float,
    private val pillHeight: Float,
    private val pillR: Float
) : View(context) {

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rect = RectF()

    init {
        updatePillColor()
    }

    fun updatePillColor() {
        pillPaint.color = 0x99000000.toInt()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(pillWidth.toInt(), widthMeasureSpec),
            resolveSize(pillHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = (width - pillWidth) / 2f
        val cy = (height - pillHeight) / 2f
        rect.set(cx, cy, cx + pillWidth, cy + pillHeight)

        val radius = min(pillR, pillHeight / 2f)

        canvas.drawRoundRect(rect, radius, radius, pillPaint)
    }
}
