package com.islandexplorer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.islandexplorer.camera.CameraTouchListener
import com.islandexplorer.camera.CameraViewMode
import com.islandexplorer.core.GameManager
import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3
import com.islandexplorer.input.AndroidTouchInputHandler
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        
        gameManager = GameManager()
        gameManager.initGame()
        
        gameView = GameView(this, gameManager)
        setContentView(gameView)
    }
}

class GameView(
    context: Context, 
    private val gameManager: GameManager
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var isRunning = false
    
    // Joystick State
    private var joyCenterX = 250f
    private var joyCenterY = 0f
    private val joyRadius = 140f
    private var joyInput = Vector2(0f, 0f)
    private var joyPointerId = -1

    // Status Message Toast
    private var statusMessage: String = "Explore the island in 3D!"
    private var statusTimer: Float = 5.0f
    private var playerAnimPhase: Float = 0f

    // Camera Touch Handler
    private val touchHandler = AndroidTouchInputHandler(
        context,
        gameManager.gameCamera,
        gameManager.viewTransitionManager,
        gameManager.terrainManager,
        gameManager.raycaster,
        object : CameraTouchListener {
            override fun onWorldTargetTapped(worldPoint: Vector3, screenPoint: Vector2) {
                gameManager.waypointTarget = worldPoint
                gameManager.waypointTimer = 3.0f
                statusMessage = "Target: (${worldPoint.x.toInt()}, ${worldPoint.y.toInt()}m, ${worldPoint.z.toInt()})"
                statusTimer = 3.0f
            }

            override fun onViewModeToggled(newMode: CameraViewMode) {
                // View mode switching removed
            }
        }
    )

    // Graphical Paints
    private val terrainPaint = Paint().apply { isAntiAlias = true }
    private val terrainEdgePaint = Paint().apply {
        color = Color.argb(40, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    private val shadowPaint = Paint().apply { 
        color = Color.argb(120, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val playerPaint = Paint().apply { 
        color = Color.rgb(70, 150, 255)
        style = Paint.Style.FILL
    }
    private val playerHeadPaint = Paint().apply { 
        color = Color.rgb(255, 215, 120)
        style = Paint.Style.FILL
    }
    private val playerLimbPaint = Paint().apply { 
        color = Color.rgb(50, 120, 220)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val waypointPaint = Paint().apply {
        color = Color.argb(220, 255, 80, 80)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val waypointFillPaint = Paint().apply {
        color = Color.argb(90, 255, 80, 80)
        style = Paint.Style.FILL
    }
    private val fogPaint = Paint()
    private val uiPaint = Paint().apply { 
        color = Color.WHITE
        textSize = 34f
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    // Reusable buffers for projection
    private val p00 = Vector2(0f, 0f)
    private val p10 = Vector2(0f, 0f)
    private val p11 = Vector2(0f, 0f)
    private val p01 = Vector2(0f, 0f)
    private val playerScreen = Vector2(0f, 0f)
    private val playerHeadScreen = Vector2(0f, 0f)
    private val waypointScreen = Vector2(0f, 0f)
    private val tilePath = Path()
    
    // UI Buttons
    private val btnTorchRect = RectF()
    private val btnTentRect = RectF()
    private val btnBagRect = RectF()

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        joyCenterY = height - 260f
        touchHandler.viewportWidth = width.toFloat()
        touchHandler.viewportHeight = height.toFloat()
        isRunning = true
        thread = Thread(this).apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        joyCenterY = height - 260f
        touchHandler.viewportWidth = width.toFloat()
        touchHandler.viewportHeight = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        isRunning = false
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (isRunning) {
            if (!holder.surface.isValid) continue
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now

            updateLogic(dt)
            drawFrame()
        }
    }

    private fun updateLogic(dt: Float) {
        gameManager.inputManager.virtualJoystickInput = joyInput
        gameManager.update()

        if (statusTimer > 0f) {
            statusTimer -= dt
        }
    }

    private fun drawFrame() {
        val canvas: Canvas? = holder.lockCanvas()
        canvas?.let { c ->
            try {
                val screenW = width.toFloat()
                val screenH = height.toFloat()

                // Sky clear color based on day/night
                val ambient = gameManager.dayNightSystem.getAmbientLight()
                val skyR = (25 * ambient).toInt()
                val skyG = (35 * ambient).toInt()
                val skyB = (60 * ambient).toInt()
                c.drawColor(Color.argb(255, skyR, skyG, skyB))

                val camera = gameManager.gameCamera
                camera.updateMatrices(screenW, screenH)

                val movement = gameManager.movement
                val terrain = gameManager.terrainManager
                val px = movement.position.x
                val py = movement.position.y
                val pz = movement.position.z

                // 1. Render 3D Perspective Terrain Grid
                val tileSize = 8.0f
                val gridRadius = 16
                val centerTileX = (camera.target.x / tileSize).toInt()
                val centerTileZ = (camera.target.z / tileSize).toInt()

                // Draw tiles from back to front based on camera pitch
                val startZ = centerTileZ - gridRadius
                val endZ = centerTileZ + gridRadius
                val startX = centerTileX - gridRadius
                val endX = centerTileX + gridRadius

                val tileIndices = mutableListOf<Pair<Int, Int>>()
                for (tz in startZ..endZ) {
                    for (tx in startX..endX) {
                        tileIndices.add(Pair(tx, tz))
                    }
                }

                // Sort tiles back-to-front relative to camera position for painter's depth ordering
                val camX = camera.position.x
                val camZ = camera.position.z
                tileIndices.sortByDescending { (tx, tz) ->
                    val mx = tx * tileSize + tileSize * 0.5f
                    val mz = tz * tileSize + tileSize * 0.5f
                    val dx = mx - camX
                    val dz = mz - camZ
                    dx * dx + dz * dz
                }

                for ((tx, tz) in tileIndices) {
                    val wx0 = tx * tileSize
                    val wz0 = tz * tileSize
                    val wx1 = wx0 + tileSize
                    val wz1 = wz0 + tileSize

                    val wy00 = terrain.getElevationAt(wx0, wz0)
                    val wy10 = terrain.getElevationAt(wx1, wz0)
                    val wy11 = terrain.getElevationAt(wx1, wz1)
                    val wy01 = terrain.getElevationAt(wx0, wz1)

                    val ok00 = camera.worldToScreen(wx0, wy00, wz0, screenW, screenH, p00)
                    val ok10 = camera.worldToScreen(wx1, wy10, wz0, screenW, screenH, p10)
                    val ok11 = camera.worldToScreen(wx1, wy11, wz1, screenW, screenH, p11)
                    val ok01 = camera.worldToScreen(wx0, wy01, wz1, screenW, screenH, p01)

                    // If quad is visible in front of camera
                    if (ok00 && ok10 && ok11 && ok01) {
                        val avgElevation = (wy00 + wy10 + wy11 + wy01) * 0.25f

                        // Base palette
                        val baseColor = when {
                            avgElevation < -0.5f -> Color.rgb(30, 144, 255) // Water
                            avgElevation < 1.0f  -> Color.rgb(238, 214, 175) // Sand
                            avgElevation < 7.0f  -> Color.rgb(40, 160, 50)   // Grass
                            avgElevation < 12.0f -> Color.rgb(139, 90, 45)   // Mud/Rock
                            else                 -> Color.rgb(170, 170, 180) // Peak
                        }

                        // Lighting calculation
                        val shade = (avgElevation * 5.0f).toInt().coerceIn(-40, 50)
                        val rRaw = (((baseColor shr 16) and 0xFF) + shade) * ambient
                        val gRaw = (((baseColor shr 8) and 0xFF) + shade) * ambient
                        val bRaw = ((baseColor and 0xFF) + shade) * ambient

                        terrainPaint.color = Color.argb(
                            255,
                            rRaw.toInt().coerceIn(0, 255),
                            gRaw.toInt().coerceIn(0, 255),
                            bRaw.toInt().coerceIn(0, 255)
                        )

                        tilePath.reset()
                        tilePath.moveTo(p00.x, p00.y)
                        tilePath.lineTo(p10.x, p10.y)
                        tilePath.lineTo(p11.x, p11.y)
                        tilePath.lineTo(p01.x, p01.y)
                        tilePath.close()

                        c.drawPath(tilePath, terrainPaint)
                        c.drawPath(tilePath, terrainEdgePaint)
                    }
                }

                // 2. Render Tap Waypoint (if active)
                gameManager.waypointTarget?.let { wp ->
                    if (camera.worldToScreen(wp.x, wp.y, wp.z, screenW, screenH, waypointScreen)) {
                        val pulse = (1.0f - (gameManager.waypointTimer / 3.0f)).coerceIn(0f, 1f)
                        val ringRadius = 15f + pulse * 45f
                        c.drawCircle(waypointScreen.x, waypointScreen.y, ringRadius, waypointPaint)
                        c.drawCircle(waypointScreen.x, waypointScreen.y, 10f, waypointFillPaint)
                    }
                }

                // 3. Render Player in 3D Space
                val okFeet = camera.worldToScreen(px, py, pz, screenW, screenH, playerScreen)
                val okHead = camera.worldToScreen(px, py + 2.0f, pz, screenW, screenH, playerHeadScreen)

                if (okFeet && okHead) {
                    val pHeight = (playerScreen.y - playerHeadScreen.y).coerceAtLeast(10f)
                    val pWidth = pHeight * 0.45f
                    val cx = playerScreen.x
                    
                    // Update animation phase
                    val currentSpeed = movement.currentSpeed
                    playerAnimPhase += currentSpeed * 0.1f // scales with speed
                    
                    // Swing multipliers based on speed
                    val speedNorm = (currentSpeed / 6.0f).coerceIn(0f, 1f)
                    val swing = Math.sin(playerAnimPhase.toDouble()).toFloat() * speedNorm
                    val swingCos = Math.cos(playerAnimPhase.toDouble()).toFloat() * speedNorm

                    // Calculate body landmarks
                    val headRadius = pWidth * 0.4f
                    val headY = playerHeadScreen.y + headRadius
                    val shoulderY = headY + headRadius * 1.2f
                    val pelvisY = playerScreen.y - pHeight * 0.35f
                    val limbWidth = pWidth * 0.25f

                    // Drop shadow on ground
                    c.drawOval(
                        cx - pWidth * 1.1f,
                        playerScreen.y - pWidth * 0.4f,
                        cx + pWidth * 1.1f,
                        playerScreen.y + pWidth * 0.4f,
                        shadowPaint
                    )

                    playerLimbPaint.strokeWidth = limbWidth

                    // Back Arm & Leg (Draw first so they are behind body)
                    // Arm: Shoulder to Hand
                    val backHandX = cx - pWidth * 0.6f + swing * pWidth * 0.8f
                    val backHandY = shoulderY + (pelvisY - shoulderY) * 0.8f - Math.abs(swing) * pWidth * 0.3f
                    c.drawLine(cx, shoulderY, backHandX, backHandY, playerLimbPaint)
                    c.drawCircle(backHandX, backHandY, limbWidth / 2, playerHeadPaint)

                    // Leg: Pelvis to Knee to Foot
                    val backKneeX = cx + pWidth * 0.2f - swing * pWidth * 0.5f
                    val backKneeY = pelvisY + (playerScreen.y - pelvisY) * 0.5f - Math.max(0f, swingCos) * pWidth * 0.3f
                    val backFootX = cx + pWidth * 0.2f - swing * pWidth * 0.9f
                    val backFootY = playerScreen.y - Math.max(0f, swingCos) * pWidth * 0.6f
                    c.drawLine(cx, pelvisY, backKneeX, backKneeY, playerLimbPaint)
                    c.drawLine(backKneeX, backKneeY, backFootX, backFootY, playerLimbPaint)
                    c.drawCircle(backFootX, backFootY, limbWidth / 2, playerPaint)

                    // Player Body Trunk
                    val bodyRect = RectF(
                        cx - pWidth * 0.4f,
                        shoulderY,
                        cx + pWidth * 0.4f,
                        pelvisY
                    )
                    c.drawRoundRect(bodyRect, pWidth * 0.3f, pWidth * 0.3f, playerPaint)

                    // Front Leg: Pelvis to Knee to Foot
                    val frontKneeX = cx - pWidth * 0.2f + swing * pWidth * 0.5f
                    val frontKneeY = pelvisY + (playerScreen.y - pelvisY) * 0.5f - Math.max(0f, -swingCos) * pWidth * 0.3f
                    val frontFootX = cx - pWidth * 0.2f + swing * pWidth * 0.9f
                    val frontFootY = playerScreen.y - Math.max(0f, -swingCos) * pWidth * 0.6f
                    c.drawLine(cx, pelvisY, frontKneeX, frontKneeY, playerLimbPaint)
                    c.drawLine(frontKneeX, frontKneeY, frontFootX, frontFootY, playerLimbPaint)
                    c.drawCircle(frontFootX, frontFootY, limbWidth / 2, playerPaint)

                    // Front Arm: Shoulder to Hand
                    val frontHandX = cx + pWidth * 0.6f - swing * pWidth * 0.8f
                    val frontHandY = shoulderY + (pelvisY - shoulderY) * 0.8f - Math.abs(swing) * pWidth * 0.3f
                    c.drawLine(cx, shoulderY, frontHandX, frontHandY, playerLimbPaint)
                    c.drawCircle(frontHandX, frontHandY, limbWidth / 2, playerHeadPaint)

                    // Player Head
                    c.drawCircle(cx, headY, headRadius, playerHeadPaint)
                }

                // 4. Distance / Atmospheric Fog Layer
                val weather = gameManager.weatherSystem.currentWeather
                fogPaint.color = Color.argb(
                    ((weather.fogDensity * 0.4f) * 255).toInt(), 
                    weather.r, weather.g, weather.b
                )
                c.drawRect(0f, 0f, screenW, screenH, fogPaint)

                // 4.5 Darkness & Torch Overlay
                val darknessAlpha = ((1.0f - ambient) * 220).toInt().coerceIn(0, 255)
                if (darknessAlpha > 0) {
                    if (gameManager.torchSystem.isEquipped && gameManager.torchSystem.batteryLevel > 0f) {
                        val cx = screenW / 2f
                        val cy = screenH / 2f
                        val radius = screenW.coerceAtMost(screenH) * 0.65f
                        val gradient = android.graphics.RadialGradient(
                            cx, cy, radius,
                            intArrayOf(Color.TRANSPARENT, Color.argb(darknessAlpha, 0, 0, 0)),
                            floatArrayOf(0.2f, 1.0f),
                            android.graphics.Shader.TileMode.CLAMP
                        )
                        c.drawRect(0f, 0f, screenW, screenH, Paint().apply { shader = gradient })
                    } else {
                        c.drawRect(0f, 0f, screenW, screenH, Paint().apply { color = Color.argb(darknessAlpha, 0, 0, 0) })
                    }
                }

                // 5. UI HUD & Camera Mode Switch Button
                drawUI(c, screenW, screenH)

                // 6. Virtual Joystick
                val joyBasePaint = Paint().apply { color = Color.argb(110, 255, 255, 255) }
                c.drawCircle(joyCenterX, joyCenterY, joyRadius, joyBasePaint)
                val nubX = joyCenterX + (joyInput.x * joyRadius)
                val nubY = joyCenterY + (joyInput.y * joyRadius)
                c.drawCircle(nubX, nubY, 55f, Paint().apply { color = Color.argb(220, 255, 255, 255) })

            } finally {
                holder.unlockCanvasAndPost(c)
            }
        }
    }

    private fun drawUI(c: Canvas, screenW: Float, screenH: Float) {
        val cam = gameManager.gameCamera
        
        // HUD Info
        c.drawText("Elevation: ${String.format("%.1f", gameManager.movement.position.y)}m", 45f, 90f, uiPaint)
        c.drawText("Stamina: ${gameManager.stamina.currentStamina.toInt()}%", 45f, 140f, uiPaint)
        
        val hours = gameManager.dayNightSystem.timeOfDay.toInt()
        val mins = ((gameManager.dayNightSystem.timeOfDay - hours) * 60).toInt()
        val timeStr = String.format("%02d:%02d", hours, mins)
        val weatherStr = gameManager.weatherSystem.currentWeather.name
        val heading = gameManager.compassSystem.getHeading(cam.yaw)
        
        c.drawText("Time: $timeStr | Weather: $weatherStr", 45f, 190f, uiPaint)
        c.drawText("Heading: $heading | Map Explored: ${if(gameManager.mapSystem.isExplored(gameManager.movement.position.x, gameManager.movement.position.z)) "Yes" else "No"}", 45f, 240f, uiPaint)

        // Draw Right Side Action Buttons
        val btnW = 200f
        val btnH = 80f
        val btnX = screenW - btnW - 40f
        
        val btnPaint = Paint().apply { color = Color.argb(180, 40, 40, 50); style = Paint.Style.FILL; isAntiAlias = true }
        val btnTextPaint = Paint().apply { color = Color.WHITE; textSize = 32f; isAntiAlias = true; textAlign = Paint.Align.CENTER }
        
        // Torch Button
        btnTorchRect.set(btnX, 50f, btnX + btnW, 50f + btnH)
        c.drawRoundRect(btnTorchRect, 20f, 20f, btnPaint)
        val torchLabel = if (gameManager.torchSystem.isEquipped) "Torch OFF" else "Torch ON"
        c.drawText(torchLabel, btnTorchRect.centerX(), btnTorchRect.centerY() + 10f, btnTextPaint)
        c.drawText("${gameManager.torchSystem.batteryLevel.toInt()}%", btnTorchRect.centerX(), btnTorchRect.centerY() + 45f, Paint(btnTextPaint).apply { textSize = 20f; color = Color.LTGRAY })
        
        // Tent/Rest Button
        val tentY = 50f + btnH + 40f
        btnTentRect.set(btnX, tentY, btnX + btnW, tentY + btnH)
        c.drawRoundRect(btnTentRect, 20f, 20f, btnPaint)
        val tentLabel = if (gameManager.tentSystem.isDeployed) "Rest" else "Deploy Tent"
        c.drawText(tentLabel, btnTentRect.centerX(), btnTentRect.centerY() + 10f, btnTextPaint)

        // Backpack Button
        val bagY = tentY + btnH + 40f
        btnBagRect.set(btnX, bagY, btnX + btnW, bagY + btnH)
        c.drawRoundRect(btnBagRect, 20f, 20f, btnPaint)
        c.drawText("Backpack", btnBagRect.centerX(), btnBagRect.centerY() + 10f, btnTextPaint)

        // Status Toast or Hint
        if (statusTimer > 0f) {
            val toastPaint = Paint().apply {
                color = Color.argb(180, 20, 20, 20)
                style = Paint.Style.FILL
            }
            val toastTextPaint = Paint().apply {
                color = Color.rgb(255, 230, 100)
                textSize = 30f
                isAntiAlias = true
            }
            val tw = toastTextPaint.measureText(statusMessage)
            val toastRect = RectF((screenW - tw) / 2f - 30f, 230f, (screenW + tw) / 2f + 30f, 300f)
            c.drawRoundRect(toastRect, 20f, 20f, toastPaint)
            c.drawText(statusMessage, (screenW - tw) / 2f, 275f, toastTextPaint)
        }

        // Gesture guide at bottom right
        val hintPaint = Paint().apply {
            color = Color.argb(160, 255, 255, 255)
            textSize = 24f
            isAntiAlias = true
        }
        c.drawText("Pan: 1-Finger | Zoom: Pinch | Orbit: 2-Finger", screenW - 480f, screenH - 50f, hintPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        // 1. Check UI Buttons First
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (btnTorchRect.contains(x, y)) {
                gameManager.torchSystem.toggleTorch()
                return true
            }
            if (btnTentRect.contains(x, y)) {
                if (gameManager.tentSystem.isDeployed) {
                    gameManager.tentSystem.restUntilNextShift()
                    gameManager.tentSystem.packTent()
                    statusMessage = "You rested until the next shift."
                    statusTimer = 3.0f
                } else {
                    gameManager.tentSystem.deployTent()
                    statusMessage = "Tent deployed."
                    statusTimer = 3.0f
                }
                return true
            }
            if (btnBagRect.contains(x, y)) {
                gameManager.backpackSystem.toggleBackpack()
                statusMessage = if (gameManager.backpackSystem.isOpen) "Backpack Opened" else "Backpack Closed"
                statusTimer = 2.0f
                return true
            }
        }

        // 2. Check Joystick Region (Bottom Left)
        val inJoyZone = x < width * 0.45f && y > height * 0.55f
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (inJoyZone && joyPointerId == -1) {
                joyPointerId = pointerId
                updateJoystick(x, y)
                return true
            }
        }

        if (action == MotionEvent.ACTION_MOVE) {
            for (i in 0 until event.pointerCount) {
                if (event.getPointerId(i) == joyPointerId) {
                    updateJoystick(event.getX(i), event.getY(i))
                }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
            if (pointerId == joyPointerId) {
                joyPointerId = -1
                joyInput = Vector2(0f, 0f)
            }
        }

        // 3. Forward touch events to 3D Camera Touch Handler
        return touchHandler.onTouch(this, event)
    }

    private fun updateJoystick(x: Float, y: Float) {
        var dx = x - joyCenterX
        var dy = y - joyCenterY
        val dist = sqrt(dx * dx + dy * dy)
        
        if (dist > joyRadius) {
            dx = (dx / dist) * joyRadius
            dy = (dy / dist) * joyRadius
        }
        
        joyInput = Vector2(dx / joyRadius, dy / joyRadius)
    }
}
