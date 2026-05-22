package com.example.flappybird

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Bird(screenWidth: Int, screenHeight: Int) {

    val x: Float = screenWidth * 0.25f
    var y: Float = screenHeight * 0.45f

    val radius: Float = screenWidth * 0.045f

    private var velocityY: Float = 0f
    private val gravity: Float = screenHeight * 0.0018f
    private val flapStrength: Float = -screenHeight * 0.018f

    // Wing animation
    private var wingAngle: Float = 0f
    private var wingDirection: Float = 1f

    private val bodyPaint = Paint().apply {
        color = Color.parseColor("#FFD700") // Golden yellow
        isAntiAlias = true
    }
    private val bellyPaint = Paint().apply {
        color = Color.parseColor("#FFF8DC")
        isAntiAlias = true
    }
    private val eyePaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    private val pupilPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }
    private val beakPaint = Paint().apply {
        color = Color.parseColor("#FF8C00")
        isAntiAlias = true
    }
    private val wingPaint = Paint().apply {
        color = Color.parseColor("#FFC200")
        isAntiAlias = true
    }
    private val outlinePaint = Paint().apply {
        color = Color.parseColor("#CC8800")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    fun flap() {
        velocityY = flapStrength
        wingAngle = -30f
    }

    fun update() {
        velocityY += gravity
        y += velocityY

        // Wing flap animation
        wingAngle += wingDirection * 4f
        if (wingAngle > 20f) wingDirection = -1f
        if (wingAngle < -20f) wingDirection = 1f
    }

    fun draw(canvas: Canvas) {
        val cx = x
        val cy = y

        // Wing (behind body)
        canvas.save()
        canvas.rotate(wingAngle, cx - radius * 0.3f, cy)
        canvas.drawOval(
            RectF(cx - radius * 1.3f, cy - radius * 0.5f, cx - radius * 0.1f, cy + radius * 0.3f),
            wingPaint
        )
        canvas.restore()

        // Body
        canvas.drawCircle(cx, cy, radius, bodyPaint)
        canvas.drawCircle(cx, cy, radius, outlinePaint)

        // Belly
        canvas.drawOval(
            RectF(cx - radius * 0.4f, cy - radius * 0.2f, cx + radius * 0.7f, cy + radius * 0.7f),
            bellyPaint
        )

        // Eye white
        canvas.drawCircle(cx + radius * 0.35f, cy - radius * 0.2f, radius * 0.28f, eyePaint)
        // Pupil
        canvas.drawCircle(cx + radius * 0.42f, cy - radius * 0.15f, radius * 0.14f, pupilPaint)

        // Beak
        val beakPath = android.graphics.Path()
        beakPath.moveTo(cx + radius * 0.7f, cy + radius * 0.1f)
        beakPath.lineTo(cx + radius * 1.2f, cy + radius * 0.0f)
        beakPath.lineTo(cx + radius * 0.7f, cy + radius * 0.3f)
        beakPath.close()
        canvas.drawPath(beakPath, beakPaint)
    }

    fun getBounds(): RectF {
        return RectF(x - radius * 0.8f, y - radius * 0.8f, x + radius * 0.8f, y + radius * 0.8f)
    }

    fun getVelocityY(): Float = velocityY
}
