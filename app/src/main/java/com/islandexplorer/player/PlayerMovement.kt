package com.islandexplorer.player

import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3

class PlayerMovement {
    var position = Vector3(0f, 0f, 0f)
    var yaw = 0f // Player's rotation around Y axis
    var currentSpeed = 0f
    
    private val walkSpeed = 3.5f
    private val runSpeed = 8.0f
    private val acceleration = 8.0f
    private var _isWalking = false
    private var _isRunning = false
    private var state = PlayerState.IDLE

    fun move(input: Vector2, wantToRun: Boolean, isTired: Boolean, deltaTime: Float) {
        // vaxis (vertical) drives forward/back. haxis (horizontal) steers.
        val vaxis = -input.y // joystick up is negative Y
        val haxis = input.x

        val targetSpeed = if (Math.abs(vaxis) > 0.1f) {
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
        
        // Handle tank rotation (Driving controls)
        if ((Math.abs(vaxis) > 0.1f || Math.abs(haxis) > 0.1f)) {
            val rotationSpeed = 150f
            if (vaxis >= 0) {
                yaw += haxis * rotationSpeed * deltaTime
            } else {
                yaw -= haxis * rotationSpeed * deltaTime
            }
            yaw = (yaw % 360f + 360f) % 360f
        }

        // Apply movement vector in the direction of the player's yaw
        if (currentSpeed > 0.1f) {
            val yawRad = Math.toRadians(yaw.toDouble())
            val forwardX = -Math.sin(yawRad).toFloat()
            val forwardZ = -Math.cos(yawRad).toFloat()
            
            val moveMult = if (vaxis < 0) -1f else 1f
            position.x += forwardX * currentSpeed * moveMult * deltaTime
            position.z += forwardZ * currentSpeed * moveMult * deltaTime
        }
    }
    
    fun isWalking() = _isWalking
    fun isRunning() = _isRunning
    fun getCurrentState() = state
}
enum class PlayerState { IDLE, WALKING, RUNNING, TIRED, JUMPING, CROUCHING }
