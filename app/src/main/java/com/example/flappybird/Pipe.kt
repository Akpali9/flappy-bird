package com.example.flappybird

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader

class Pipe(
    var x: Float,
    private val gapTop: Float,
    private val gapBottom: Float,
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    val width: Float = screenWidth * 0.15f
    var passed: Boolean = false
    private val speed: Float = screenWidth * 0.004f

    private val pipeGreenDark = Color.parseColor("#2E7D32")
    private val pipeGreenLight = Color.parseColor("#66BB6A")
    private val pipeDarkEdge = Color.parseColor("#1B5E20")
    private val capColor = Color.parseColor("#388E3C")

    private val capHeight = screenHeight * 0.04f
    private val capExtraWidth = screenWidth * 0.02f

    private val bodyPaint = Paint().apply {
        isAntiAlias = true
    }
    private val capPaint = Paint().apply {
        color = capColor
        isAntiAlias = true
    }
    private val outlinePaint = Paint().apply {
        color = pipeDarkEdge
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    fun update() {
        x -= speed
    }

    fun draw(canvas: Canvas) {
        // ---- TOP PIPE (hangs from top) ----
        val topBodyRect = RectF(x, 0f, x + width, gapTop - capHeight)
        val topCapRect = RectF(x - capExtraWidth, gapTop - capHeight, x + width + capExtraWidth, gapTop)

        // Body gradient
        bodyPaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(pipeGreenDark, pipeGreenLight, pipeGreenLight, pipeGreenDark),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(topBodyRect, bodyPaint)
        canvas.drawRect(topBodyRect, outlinePaint)

        // Cap
        capPaint.shader = LinearGradient(
            topCapRect.left, 0f, topCapRect.right, 0f,
            intArrayOf(pipeGreenDark, pipeGreenLight, pipeGreenLight, pipeGreenDark),
            floatArrayOf(0f, 0.25f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(topCapRect, 8f, 8f, capPaint)
        canvas.drawRoundRect(topCapRect, 8f, 8f, outlinePaint)

        // ---- BOTTOM PIPE ----
        val bottomBodyRect = RectF(x, gapBottom + capHeight, x + width, screenHeight.toFloat())
        val bottomCapRect = RectF(x - capExtraWidth, gapBottom, x + width + capExtraWidth, gapBottom + capHeight)

        bodyPaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(pipeGreenDark, pipeGreenLight, pipeGreenLight, pipeGreenDark),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(bottomBodyRect, bodyPaint)
        canvas.drawRect(bottomBodyRect, outlinePaint)

        capPaint.shader = LinearGradient(
            bottomCapRect.left, 0f, bottomCapRect.right, 0f,
            intArrayOf(pipeGreenDark, pipeGreenLight, pipeGreenLight, pipeGreenDark),
            floatArrayOf(0f, 0.25f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(bottomCapRect, 8f, 8f, capPaint)
        canvas.drawRoundRect(bottomCapRect, 8f, 8f, outlinePaint)
    }

    fun getTopRect(): RectF = RectF(x, 0f, x + width, gapTop)
    fun getBottomRect(): RectF = RectF(x, gapBottom, x + width, screenHeight.toFloat())

    fun isOffScreen(): Boolean = x + width < 0
}
