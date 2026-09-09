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
import com.islandexplorer.core.Vector3
import com.islandexplorer.player.PlayerMovement
import com.islandexplorer.player.PlayerStamina
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide action bar for full screen
        supportActionBar?.hide()
        
        // Initialize Core Systems (so we can pass refs to the UI)
        val movement = PlayerMovement()
        val stamina = PlayerStamina()
        gameManager = GameManager() // In a real app we'd inject dependencies
        
        gameView = GameView(this, movement, stamina)
        setContentView(gameView)
        
        gameManager.initGame()
    }
}

class GameView(
    context: Context, 
    private val movement: PlayerMovement,
    private val stamina: PlayerStamina
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var isRunning = false
    
    // Joystick State
    private var joyCenterX = 250f
    private var joyCenterY = 0f // Set on size changed
    private val joyRadius = 150f
    private var joyInput = Vector2(0f, 0f)
    private var joyPointerId = -1
    
    // Virtual Camera Drag State
    private var camDragPointerId = -1
    
    // Paints
    private val bgPaint = Paint().apply { color = Color.rgb(20, 30, 20) }
    private val playerPaint = Paint().apply { color = Color.rgb(100, 150, 255) }
    private val joyBasePaint = Paint().apply { color = Color.argb(100, 255, 255, 255) }
    private val joyNubPaint = Paint().apply { color = Color.argb(200, 255, 255, 255) }
    private val textPaint = Paint().apply { 
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
    }

    private var lastTime = System.nanoTime()

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

            val currentTime = System.nanoTime()
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime

            updateLogic(dt)
            drawFrame()
        }
    }

    private fun updateLogic(dt: Float) {
        // Feed joystick to movement
        movement.move(joyInput, false, stamina.isTired(), dt)
        stamina.update(dt, movement.isWalking(), movement.isRunning())
    }

    private fun drawFrame() {
        val canvas: Canvas? = holder.lockCanvas()
        canvas?.let {
            try {
                it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                // 1. Draw 2D Top-Down Player Representation
                val scale = 20f
                val drawX = (width / 2f) + (movement.position.x * scale)
                val drawY = (height / 2f) - (movement.position.z * scale) // -Z is up in 3D
                it.drawCircle(drawX, drawY, 30f, playerPaint)
                
                // 2. HUD
                it.drawText("Vertical 3D Prototype Engine", 50f, 100f, textPaint)
                it.drawText("State: \${movement.getCurrentState()}", 50f, 160f, textPaint)
                it.drawText("Stamina: \${stamina.currentStamina.toInt()}/100", 50f, 220f, textPaint)
                it.drawText("X: \${movement.position.x.toInt()} Z: \${movement.position.z.toInt()}", 50f, 280f, textPaint)

                // 3. Draw Virtual Joystick
                it.drawCircle(joyCenterX, joyCenterY, joyRadius, joyBasePaint)
                val nubX = joyCenterX + (joyInput.x * joyRadius)
                val nubY = joyCenterY + (joyInput.y * joyRadius)
                it.drawCircle(nubX, nubY, 60f, joyNubPaint)
                
                // 4. Draw Right Side Camera Drag Area Hint
                it.drawText("Drag Here for Camera", width - 450f, height - 300f, textPaint)

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
                // Check if touch is on left side (Joystick)
                if (x < width / 2f && joyPointerId == -1) {
                    joyPointerId = pointerId
                    updateJoystick(x, y)
                } 
                // Right side (Camera)
                else if (x >= width / 2f && camDragPointerId == -1) {
                    camDragPointerId = pointerId
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pId = event.getPointerId(i)
                    val pX = event.getX(i)
                    val pY = event.getY(i)
                    
                    if (pId == joyPointerId) {
                        updateJoystick(pX, pY)
                    } else if (pId == camDragPointerId) {
                        // In a real implementation, send delta to TouchInputManager
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
