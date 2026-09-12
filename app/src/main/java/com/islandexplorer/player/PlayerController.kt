package com.islandexplorer.player

import com.islandexplorer.core.Vector2

class PlayerController(
    private val movement: PlayerMovement,
    private val stamina: PlayerStamina,
    private val animation: PlayerAnimation
) {
    fun update(deltaTime: Float, input: Vector2, cameraYaw: Float = 0f) {
        movement.move(input, cameraYaw, false, stamina.isTired(), deltaTime)
        stamina.update(deltaTime, movement.isWalking(), movement.isRunning())
        animation.updateAnimationState(movement.getCurrentState())
    }
}
