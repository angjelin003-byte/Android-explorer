package com.islandexplorer.input

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.islandexplorer.camera.CameraViewMode
import com.islandexplorer.camera.GameCamera
import com.islandexplorer.camera.TouchToWorldRaycaster
import com.islandexplorer.camera.ViewTransitionManager
import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3
import com.islandexplorer.world.TerrainManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

interface CameraTouchListener {
    fun onWorldTargetTapped(worldPoint: Vector3, screenPoint: Vector2)
    fun onViewModeToggled(newMode: CameraViewMode)
}

class AndroidTouchInputHandler(
    context: Context,
    private val camera: GameCamera,
    private val viewTransitionManager: ViewTransitionManager,
    private val terrainManager: TerrainManager,
    private val raycaster: TouchToWorldRaycaster,
    var touchListener: CameraTouchListener? = null
) : View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {

    private val gestureDetector = GestureDetector(context, this).apply {
        setOnDoubleTapListener(this@AndroidTouchInputHandler)
    }
    private val scaleDetector = ScaleGestureDetector(context, this)

    // Touch tracking
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var twoFingerPreviousAngle = 0f
    private var twoFingerPreviousY = 0f
    private var isTwoFingerGesture = false

    // Viewport dimensions
    var viewportWidth = 1080f
    var viewportHeight = 1920f

    // Joystick region (bottom-left) to avoid camera interference
    var joystickActive = false
    var joystickPointerId = -1

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val action = event.actionMasked
        val pointerCount = event.pointerCount

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.getX(0)
                val y = event.getY(0)

                // Ignore if in joystick zone
                if (x < viewportWidth * 0.45f && y > viewportHeight * 0.55f) {
                    joystickActive = true
                    joystickPointerId = event.getPointerId(0)
                    return false
                }

                lastTouchX = x
                lastTouchY = y
                isDragging = true
                isTwoFingerGesture = false
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (pointerCount >= 2) {
                    isTwoFingerGesture = true
                    val p0x = event.getX(0)
                    val p0y = event.getY(0)
                    val p1x = event.getX(1)
                    val p1y = event.getY(1)

                    twoFingerPreviousAngle = calculateAngle(p0x, p0y, p1x, p1y)
                    twoFingerPreviousY = (p0y + p1y) * 0.5f
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (joystickActive && pointerCount == 1) {
                    return false
                }

                if (pointerCount >= 2 && isTwoFingerGesture && !scaleDetector.isInProgress) {
                    // Two-Finger: Rotation (Twist) and Pitch (Vertical Drag)
                    val p0x = event.getX(0)
                    val p0y = event.getY(0)
                    val p1x = event.getX(1)
                    val p1y = event.getY(1)

                    val currentAngle = calculateAngle(p0x, p0y, p1x, p1y)
                    var angleDiff = currentAngle - twoFingerPreviousAngle

                    // Normalize angle diff to [-180, 180]
                    if (angleDiff > 180f) angleDiff -= 360f
                    if (angleDiff < -180f) angleDiff += 360f

                    // Rotate Yaw around target point
                    camera.yaw = (camera.yaw + angleDiff * 0.8f) % 360.0f
                    twoFingerPreviousAngle = currentAngle

                    // Vertical two-finger drag: Pitch adjustment
                    val currentMidY = (p0y + p1y) * 0.5f
                    val deltaY = currentMidY - twoFingerPreviousY
                    camera.pitch = (camera.pitch - deltaY * 0.15f).coerceIn(15.0f, 88.0f)
                    twoFingerPreviousY = currentMidY

                    camera.updateOrbitPosition()
                    view?.invalidate()
                } else if (pointerCount == 1 && isDragging && !joystickActive) {
                    // One-Finger Pan / Drag: Move LookAt target across ground plane (XZ)
                    val currentX = event.getX(0)
                    val currentY = event.getY(0)

                    val deltaScreenX = currentX - lastTouchX
                    val deltaScreenY = currentY - lastTouchY

                    // Sensitivity scaled by camera distance and height so drag speed is consistent 1:1 on ground
                    val panFactor = (camera.distance / viewportHeight) * 1.5f

                    // Camera orientation vectors on ground plane
                    val yawRad = Math.toRadians(camera.yaw.toDouble())
                    val forwardX = -sin(yawRad).toFloat()
                    val forwardZ = -cos(yawRad).toFloat()
                    val rightX = cos(yawRad).toFloat()
                    val rightZ = -sin(yawRad).toFloat()

                    // Screen drag mapped to camera-aligned ground plane
                    val moveX = (-deltaScreenX * rightX + deltaScreenY * forwardX) * panFactor
                    val moveZ = (-deltaScreenX * rightZ + deltaScreenY * forwardZ) * panFactor

                    camera.target.x += moveX
                    camera.target.z += moveZ

                    // Keep target elevation anchored to terrain surface
                    camera.target.y = terrainManager.getElevationAt(camera.target.x, camera.target.z)

                    camera.updateOrbitPosition()

                    lastTouchX = currentX
                    lastTouchY = currentY
                    view?.invalidate()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (pointerCount <= 2) {
                    isTwoFingerGesture = false
                    // Reset single finger drag reference
                    val activeIndex = if (event.actionIndex == 0) 1 else 0
                    lastTouchX = event.getX(activeIndex)
                    lastTouchY = event.getY(activeIndex)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                isTwoFingerGesture = false
                joystickActive = false
                joystickPointerId = -1
            }
        }

        return true
    }

    // ================= ScaleGestureDetector (Pinch to Zoom) =================
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor

        if (viewTransitionManager.currentMode == CameraViewMode.PERSPECTIVE_3D) {
            // In 3D perspective: Zoom changes camera distance & subtle FOV
            val newDistance = (camera.distance / scaleFactor).coerceIn(6.0f, 65.0f)
            camera.distance = newDistance
            camera.fov = (camera.fov - (scaleFactor - 1.0f) * 15.0f).coerceIn(30.0f, 80.0f)
        } else {
            // In Top-down: Adjusts altitude & distance
            val newDistance = (camera.distance / scaleFactor).coerceIn(15.0f, 90.0f)
            camera.distance = newDistance
        }

        camera.updateOrbitPosition()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    // ================= GestureDetector (Tap & Double Tap) =================
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        // Intersect screen coordinate with world terrain
        val hitPoint = raycaster.raycastTerrain(
            e.x, e.y,
            viewportWidth, viewportHeight,
            camera,
            terrainManager
        )

        hitPoint?.let {
            touchListener?.onWorldTargetTapped(it, Vector2(e.x, e.y))
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        viewTransitionManager.toggleViewMode(0.8f)
        touchListener?.onViewModeToggled(viewTransitionManager.targetMode)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean = false
    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false

    private fun calculateAngle(p0x: Float, p0y: Float, p1x: Float, p1y: Float): Float {
        val radians = atan2((p1y - p0y).toDouble(), (p1x - p0x).toDouble())
        return Math.toDegrees(radians).toFloat()
    }
}
