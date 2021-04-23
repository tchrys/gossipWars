package com.example.gossipwars.ui.chat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.gossipwars.logic.entities.GameHelper

class AllianceCircle(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var drawPaint: Paint
    private lateinit var canvas: Canvas

    init {
        setupPaint()
    }

    private fun setupPaint() {
        drawPaint = Paint()
        drawPaint.color = Color.BLUE
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 5F
        drawPaint.style = Paint.Style.FILL_AND_STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        canvas.translate(width /2f, height /2f);
        canvas.drawCircle(0f, 0f, 40f, drawPaint)
    }

    fun colorWithPlayerColor(idx : Int) {
        drawPaint.color = GameHelper.getColorByPlayerIdx(idx)
        invalidate()
    }

}