package com.islandexplorer.player

import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3

class PlayerMovement {
    var position = Vector3(0f, 0f, 0f)
    var currentSpeed = 0f
    
    private val walkSpeed = 2.5f
    private val runSpeed = 6.0f
    private val acceleration = 5.0f
    private var _isWalking = false
    private var _isRunning = false
    private var state = PlayerState.IDLE

    fun move(input: Vector2, cameraYaw: Float, wantToRun: Boolean, isTired: Boolean, deltaTime: Float) {
        val targetSpeed = if (input.magnitude() > 0.1f) {
            if (wantToRun && !isTired) runSpeed else walkSpeed
        } else {
            0f
        }
        
        // Smooth acceleration/deceleration
        currentSpeed += (targetSpeed - currentSpeed) * acceleration * deltaTime
        
        _isWalking = currentSpeed > 0.1f && currentSpeed <= walkSpeed + 0.5f
        _isRunning = currentSpeed > walkSpeed + 0.5f
        
        state = when {
            isTired && currentSpeed > 0.1f -> PlayerState.TIRED
            _isRunning -> PlayerState.RUNNING
            _isWalking -> PlayerState.WALKING
            else -> PlayerState.IDLE
        }
        
        // Apply movement vector relative to camera
        if (currentSpeed > 0.1f) {
            // Convert camera yaw to radians.
            // In GameCamera, yaw is 0 when camera looks down -Z axis.
            val yawRad = Math.toRadians(cameraYaw.toDouble())
            
            // Camera's forward vector in XZ plane
            val camForwardX = -Math.sin(yawRad).toFloat()
            val camForwardZ = -Math.cos(yawRad).toFloat()
            
            // Camera's right vector in XZ plane
            val camRightX = Math.cos(yawRad).toFloat()
            val camRightZ = -Math.sin(yawRad).toFloat()
            
            // input.x is Horizontal (right = positive)
            // input.y is Vertical (forward = negative, because Android screen Y goes down)
            val horizontal = input.x
            val vertical = -input.y // flip so positive means forward
            
            val moveDirX = camRightX * horizontal + camForwardX * vertical
            val moveDirZ = camRightZ * horizontal + camForwardZ * vertical
            
            // Normalize move direction so diagonal isn't faster (simplified)
            val moveLen = Math.sqrt((moveDirX * moveDirX + moveDirZ * moveDirZ).toDouble()).toFloat()
            if (moveLen > 0.001f) {
                position.x += (moveDirX / moveLen) * currentSpeed * deltaTime
                position.z += (moveDirZ / moveLen) * currentSpeed * deltaTime
            }
        }
    }
    
    fun isWalking() = _isWalking
    fun isRunning() = _isRunning
    fun getCurrentState() = state
}
enum class PlayerState { IDLE, WALKING, RUNNING, TIRED, JUMPING, CROUCHING }
