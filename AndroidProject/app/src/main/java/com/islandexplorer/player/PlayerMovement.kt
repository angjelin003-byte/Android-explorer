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

    fun move(input: Vector2, wantToRun: Boolean, isTired: Boolean, deltaTime: Float) {
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
        
        // Apply movement vector relative to camera (simplified)
        if (currentSpeed > 0.1f) {
            position.x += input.x * currentSpeed * deltaTime
            position.z += input.y * currentSpeed * deltaTime
        }
    }
    
    fun isWalking() = _isWalking
    fun isRunning() = _isRunning
    fun getCurrentState() = state
}
enum class PlayerState { IDLE, WALKING, RUNNING, TIRED, JUMPING, CROUCHING }
