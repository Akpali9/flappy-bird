package com.example.flappybird

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val thread: GameThread
    private var gameState: GameState = GameState.WAITING

    private var bird: Bird? = null
    private val pipes = mutableListOf<Pipe>()
    private var score: Int = 0
    private var highScore: Int = 0

    private var screenWidth = 0
    private var screenHeight = 0

    // Scrolling background layers
    private var bgScrollX1 = 0f
    private var bgScrollX2 = 0f
    private var groundScrollX = 0f
    private val bgSpeed = 0.5f
    private val groundSpeed = 2f

    private var pipeSpawnTimer = 0
    private var pipeSpawnInterval = 90 // frames

    // Cloud positions
    private val clouds = mutableListOf<PointF>()

    // Paints
    private val skyPaint = Paint()
    private val groundPaint = Paint()
    private val groundLinePaint = Paint().apply {
        color = Color.parseColor("#8B6914")
        strokeWidth = 4f
    }
    private val grassPaint = Paint().apply { color = Color.parseColor("#5D8A3C") }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(6f, 2f, 2f, Color.parseColor("#333333"))
        isAntiAlias = true
    }
    private val titlePaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(8f, 3f, 3f, Color.parseColor("#333333"))
        isAntiAlias = true
    }
    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#FFFDE7")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }
    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#AA000000")
    }
    private val cardPaint = Paint().apply {
        color = Color.parseColor("#EEF7C8")
        isAntiAlias = true
    }
    private val cardStrokePaint = Paint().apply {
        color = Color.parseColor("#5D8A3C")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val labelPaint = Paint().apply {
        color = Color.parseColor("#555555")
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val valuePaint = Paint().apply {
        color = Color.parseColor("#1A1A1A")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val buttonPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        isAntiAlias = true
    }
    private val buttonStrokePaint = Paint().apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val cloudPaint = Paint().apply {
        color = Color.parseColor("#E8F5E9")
        isAntiAlias = true
        alpha = 200
    }

    private val medalGoldPaint = Paint().apply { color = Color.parseColor("#FFD700"); isAntiAlias = true }
    private val medalSilverPaint = Paint().apply { color = Color.parseColor("#C0C0C0"); isAntiAlias = true }
    private val medalBronzePaint = Paint().apply { color = Color.parseColor("#CD7F32"); isAntiAlias = true }

    enum class GameState { WAITING, PLAYING, DEAD }

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width
        screenHeight = height
        initGame()
        spawnClouds()
        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread.running = false
        thread.join()
    }

    private fun initGame() {
        bird = Bird(screenWidth, screenHeight)
        pipes.clear()
        score = 0
        pipeSpawnTimer = 0
        gameState = GameState.WAITING
    }

    private fun spawnClouds() {
        clouds.clear()
        repeat(5) {
            clouds.add(PointF(Random.nextFloat() * screenWidth, (screenHeight * 0.05f) + Random.nextFloat() * screenHeight * 0.3f))
        }
    }

    private fun spawnPipe() {
        val gapSize = screenHeight * 0.28f
        val minGapTop = screenHeight * 0.15f
        val maxGapTop = screenHeight * 0.62f
        val gapTop = minGapTop + Random.nextFloat() * (maxGapTop - minGapTop)
        pipes.add(Pipe(screenWidth.toFloat(), gapTop, gapTop + gapSize, screenWidth, screenHeight))
    }

    fun update() {
        val b = bird ?: return

        // Scroll clouds
        for (cloud in clouds) {
            cloud.x -= bgSpeed
            if (cloud.x < -screenWidth * 0.2f) {
                cloud.x = screenWidth.toFloat() + Random.nextFloat() * screenWidth * 0.5f
                cloud.y = (screenHeight * 0.05f) + Random.nextFloat() * screenHeight * 0.3f
            }
        }

        groundScrollX -= groundSpeed
        if (groundScrollX < -screenWidth) groundScrollX += screenWidth

        if (gameState == GameState.PLAYING) {
            b.update()

            // Spawn pipes
            pipeSpawnTimer++
            if (pipeSpawnTimer >= pipeSpawnInterval) {
                spawnPipe()
                pipeSpawnTimer = 0
            }

            // Update pipes
            val iter = pipes.iterator()
            while (iter.hasNext()) {
                val pipe = iter.next()
                pipe.update()
                if (pipe.isOffScreen()) { iter.remove(); continue }

                // Score
                if (!pipe.passed && pipe.x + pipe.width < b.x) {
                    pipe.passed = true
                    score++
                    if (score > highScore) highScore = score
                }

                // Collision
                val bRect = b.getBounds()
                if (RectF.intersects(bRect, pipe.getTopRect()) || RectF.intersects(bRect, pipe.getBottomRect())) {
                    gameState = GameState.DEAD
                }
            }

            // Ground & ceiling collision
            val groundY = screenHeight - screenHeight * 0.12f
            if (b.y + b.radius >= groundY || b.y - b.radius <= 0) {
                gameState = GameState.DEAD
            }
        }
    }

    fun draw(canvas: Canvas) {
        val b = bird ?: return

        // Sky gradient
        val skyShader = LinearGradient(
            0f, 0f, 0f, screenHeight.toFloat(),
            intArrayOf(Color.parseColor("#64B5F6"), Color.parseColor("#B3E5FC"), Color.parseColor("#E1F5FE")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        skyPaint.shader = skyShader
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), skyPaint)

        // Clouds
        for (cloud in clouds) {
            drawCloud(canvas, cloud.x, cloud.y, screenWidth * 0.07f)
        }

        // Pipes
        for (pipe in pipes) pipe.draw(canvas)

        // Ground
        val groundY = screenHeight - screenHeight * 0.12f
        val groundShader = LinearGradient(
            0f, groundY, 0f, screenHeight.toFloat(),
            intArrayOf(Color.parseColor("#8BC34A"), Color.parseColor("#795548")),
            floatArrayOf(0f, 0.15f),
            Shader.TileMode.CLAMP
        )
        groundPaint.shader = groundShader
        canvas.drawRect(0f, groundY, screenWidth.toFloat(), screenHeight.toFloat(), groundPaint)

        // Grass line
        canvas.drawLine(0f, groundY, screenWidth.toFloat(), groundY, groundLinePaint)

        // Bird
        b.draw(canvas)

        // Score while playing
        if (gameState == GameState.PLAYING || gameState == GameState.DEAD) {
            scorePaint.textSize = screenWidth * 0.12f
            canvas.drawText(score.toString(), screenWidth / 2f, screenHeight * 0.12f, scorePaint)
        }

        // Overlays
        when (gameState) {
            GameState.WAITING -> drawWaitingScreen(canvas)
            GameState.DEAD -> drawDeadScreen(canvas)
            else -> {}
        }
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        canvas.drawCircle(cx, cy, r, cloudPaint)
        canvas.drawCircle(cx + r * 1.2f, cy + r * 0.2f, r * 0.8f, cloudPaint)
        canvas.drawCircle(cx - r * 1.0f, cy + r * 0.3f, r * 0.7f, cloudPaint)
        canvas.drawCircle(cx + r * 0.4f, cy - r * 0.5f, r * 0.65f, cloudPaint)
    }

    private fun drawWaitingScreen(canvas: Canvas) {
        val cx = screenWidth / 2f

        titlePaint.textSize = screenWidth * 0.13f
        canvas.drawText("FLAPPY", cx, screenHeight * 0.3f, titlePaint)
        canvas.drawText("BIRD", cx, screenHeight * 0.42f, titlePaint)

        subtitlePaint.textSize = screenWidth * 0.055f
        canvas.drawText("Tap anywhere to start!", cx, screenHeight * 0.58f, subtitlePaint)

        // Tap hint animation area
        subtitlePaint.textSize = screenWidth * 0.07f
        canvas.drawText("👆", cx, screenHeight * 0.68f, subtitlePaint)
    }

    private fun drawDeadScreen(canvas: Canvas) {
        // Dim overlay
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)

        val cx = screenWidth / 2f
        val cardWidth = screenWidth * 0.78f
        val cardLeft = cx - cardWidth / 2f
        val cardRight = cx + cardWidth / 2f
        val cardTop = screenHeight * 0.22f
        val cardBottom = screenHeight * 0.72f

        // Card
        canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 28f, 28f, cardPaint)
        canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 28f, 28f, cardStrokePaint)

        // "Game Over" title
        titlePaint.textSize = screenWidth * 0.1f
        titlePaint.color = Color.parseColor("#C62828")
        canvas.drawText("GAME OVER", cx, cardTop + screenHeight * 0.09f, titlePaint)
        titlePaint.color = Color.WHITE // reset

        // Medal
        val medalCx = cx - cardWidth * 0.22f
        val medalCy = cardTop + screenHeight * 0.2f
        val medalR = screenWidth * 0.09f
        val medalPaint = when {
            score >= 20 -> medalGoldPaint
            score >= 10 -> medalSilverPaint
            score >= 5 -> medalBronzePaint
            else -> null
        }
        if (medalPaint != null) {
            canvas.drawCircle(medalCx, medalCy, medalR, medalPaint)
            val starPaint = Paint().apply { color = Color.parseColor("#FFFFFF88"); isAntiAlias = true }
            canvas.drawCircle(medalCx - medalR * 0.25f, medalCy - medalR * 0.25f, medalR * 0.3f, starPaint)
        }

        // Scores
        val scoreBoxLeft = cx + cardWidth * 0.02f
        val scoreBoxRight = cardRight - screenWidth * 0.04f
        val scoreBoxTop = cardTop + screenHeight * 0.13f
        val scoreBoxBottom = cardTop + screenHeight * 0.3f
        val sbPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); isAntiAlias = true }
        canvas.drawRoundRect(RectF(scoreBoxLeft, scoreBoxTop, scoreBoxRight, scoreBoxBottom), 16f, 16f, sbPaint)

        val scoreCx = (scoreBoxLeft + scoreBoxRight) / 2f

        labelPaint.textSize = screenWidth * 0.038f
        canvas.drawText("SCORE", scoreCx, scoreBoxTop + screenHeight * 0.065f, labelPaint)
        valuePaint.textSize = screenWidth * 0.08f
        canvas.drawText(score.toString(), scoreCx, scoreBoxTop + screenHeight * 0.14f, valuePaint)

        // Divider
        val divPaint = Paint().apply { color = Color.parseColor("#A5D6A7"); strokeWidth = 2f }
        canvas.drawLine(scoreBoxLeft, scoreBoxTop + screenHeight * 0.16f, scoreBoxRight, scoreBoxTop + screenHeight * 0.16f, divPaint)

        labelPaint.textSize = screenWidth * 0.032f
        canvas.drawText("BEST", scoreCx, scoreBoxTop + screenHeight * 0.215f, labelPaint)
        valuePaint.textSize = screenWidth * 0.065f
        canvas.drawText(highScore.toString(), scoreCx, scoreBoxTop + screenHeight * 0.285f, valuePaint)

        // Restart button
        val btnWidth = cardWidth * 0.6f
        val btnLeft = cx - btnWidth / 2f
        val btnRight = cx + btnWidth / 2f
        val btnTop = cardBottom - screenHeight * 0.125f
        val btnBottom = cardBottom - screenHeight * 0.03f
        canvas.drawRoundRect(RectF(btnLeft, btnTop, btnRight, btnBottom), 40f, 40f, buttonPaint)
        canvas.drawRoundRect(RectF(btnLeft, btnTop, btnRight, btnBottom), 40f, 40f, buttonStrokePaint)
        buttonTextPaint.textSize = screenWidth * 0.065f
        canvas.drawText("▶  PLAY AGAIN", cx, (btnTop + btnBottom) / 2f + screenWidth * 0.022f, buttonTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (gameState) {
                GameState.WAITING -> {
                    gameState = GameState.PLAYING
                    bird?.flap()
                }
                GameState.PLAYING -> bird?.flap()
                GameState.DEAD -> initGame()
            }
        }
        return true
    }

    fun pause() { thread.running = false }
    fun resume() {
        if (!thread.isAlive) {
            thread.running = true
        }
    }
}
