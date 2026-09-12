package com.islandexplorer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.islandexplorer.core.GameManager
import com.islandexplorer.core.Vector2
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
    private val joyRadius = 150f
    private var joyInput = Vector2(0f, 0f)
    private var joyPointerId = -1
    private var camDragPointerId = -1
    
    // Graphical Paints
    private val terrainPaint = Paint()
    private val shadowPaint = Paint().apply { color = Color.argb(120, 0, 0, 0) }
    private val playerPaint = Paint().apply { color = Color.rgb(100, 150, 255) }
    private val fogPaint = Paint()
    private val uiPaint = Paint().apply { 
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        joyCenterY = height - 300f
        isRunning = true
        thread = Thread(this).apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

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
        while (isRunning) {
            if (!holder.surface.isValid) continue
            updateLogic()
            drawFrame()
        }
    }

    private fun updateLogic() {
        // Map joystick to input manager and tick the whole simulation
        gameManager.inputManager.virtualJoystickInput = joyInput
        gameManager.update()
    }

    private fun drawFrame() {
        val canvas: Canvas? = holder.lockCanvas()
        canvas?.let {
            try {
                it.drawColor(Color.BLACK) // Clear

                // 1. Render Enhanced Procedural Terrain Grid (Pseudo-3D)
                val movement = gameManager.movement
                val terrain = gameManager.terrainManager
                
                val tileSize = 20f
                val gridRadius = 20
                val px = movement.position.x
                val pz = movement.position.z
                
                val startX = (px / tileSize).toInt() - gridRadius
                val startZ = (pz / tileSize).toInt() - gridRadius
                
                for (z in startZ..(startZ + gridRadius * 2)) {
                    for (x in startX..(startX + gridRadius * 2)) {
                        val worldX = x * tileSize
                        val worldZ = z * tileSize
                        
                        // Calculate mathematical height of this specific tile
                        val elevation = terrain.getElevationAt(worldX, worldZ)
                        
                        // Pick color based on elevation
                        val baseColor = when {
                            elevation < -1.0f -> Color.rgb(30, 144, 255) // Water
                            elevation < 0.5f -> Color.rgb(238, 214, 175) // Sand
                            elevation < 6.0f -> Color.rgb(34, 139, 34) // Grass
                            elevation < 9.0f -> Color.rgb(139, 69, 19) // Mud
                            else -> Color.rgb(120, 120, 120) // Stone
                        }
                        
                        // Add realistic shading (higher = brighter, lower = darker)
                        val shade: Int = (elevation * 8).toInt().coerceIn(-60, 60)
                        val baseR: Int = (baseColor shr 16) and 0xFF
                        val baseG: Int = (baseColor shr 8) and 0xFF
                        val baseB: Int = baseColor and 0xFF
                        val r: Int = (baseR + shade).coerceIn(0, 255)
                        val g: Int = (baseG + shade).coerceIn(0, 255)
                        val b: Int = (baseB + shade).coerceIn(0, 255)
                        terrainPaint.color = Color.argb(255, r, g, b)
                        
                        // Project to screen coordinates
                        val screenX = (width / 2f) + (worldX - px) * 5f
                        val screenY = (height / 2f) - (worldZ - pz) * 5f 
                        
                        it.drawRect(screenX, screenY, screenX + tileSize * 5f, screenY + tileSize * 5f, terrainPaint)
                    }
                }

                // 2. Draw Player with 3D Height Simulation
                val screenCx = width / 2f
                val screenCy = height / 2f
                
                // Draw drop shadow on the ground
                it.drawCircle(screenCx, screenCy, 20f, shadowPaint)
                
                // Draw player offset by elevation to simulate physical height climbing
                val visualYOffset = movement.position.y * 5f
                it.drawCircle(screenCx, screenCy - visualYOffset, 25f, playerPaint)

                // 3. Render Realistic Fog Overlay
                val weather = gameManager.weatherSystem.currentWeather
                fogPaint.color = Color.argb(
                    (weather.fogDensity * 255).toInt(), 
                    weather.r, weather.g, weather.b
                )
                it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fogPaint)

                // 4. UI HUD
                it.drawText("Weather: ${weather.name}", 50f, 100f, uiPaint)
                val elevationStr = String.format("%.1f", movement.position.y)
                it.drawText("Elevation: ${elevationStr}m", 50f, 160f, uiPaint)
                it.drawText("Stamina: ${gameManager.stamina.currentStamina.toInt()}%", 50f, 220f, uiPaint)

                // 5. Virtual Joystick
                val joyBasePaint = Paint().apply { color = Color.argb(100, 255, 255, 255) }
                it.drawCircle(joyCenterX, joyCenterY, joyRadius, joyBasePaint)
                val nubX = joyCenterX + (joyInput.x * joyRadius)
                val nubY = joyCenterY + (joyInput.y * joyRadius)
                it.drawCircle(nubX, nubY, 60f, Paint().apply { color = Color.argb(200, 255, 255, 255) })

            } finally {
                holder.unlockCanvasAndPost(it)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (x < width / 2f && joyPointerId == -1) {
                    joyPointerId = pointerId
                    updateJoystick(x, y)
                } else if (x >= width / 2f && camDragPointerId == -1) {
                    camDragPointerId = pointerId
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pId = event.getPointerId(i)
                    if (pId == joyPointerId) {
                        updateJoystick(event.getX(i), event.getY(i))
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (pointerId == joyPointerId) {
                    joyPointerId = -1
                    joyInput = Vector2(0f, 0f)
                } else if (pointerId == camDragPointerId) {
                    camDragPointerId = -1
                }
            }
        }
        return true
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
