package com.islandexplorer.player

class PlayerMovement {
    private var isWalking = false
    private var isRunning = false

    fun move(input: Vector2, isTired: Boolean) {
        // Implement humanoid physics-based movement
        // Check terrain collisions
    }
    fun isWalking() = isWalking
    fun isRunning() = isRunning
    fun getCurrentState(): PlayerState = PlayerState.IDLE
}
enum class PlayerState { IDLE, WALKING, RUNNING, TIRED }
class Vector2(val x: Float, val y: Float)
