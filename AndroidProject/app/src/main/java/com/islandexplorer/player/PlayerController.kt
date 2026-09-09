package com.islandexplorer.player

class PlayerController(
    private val movement: PlayerMovement,
    private val stamina: PlayerStamina,
    private val animation: PlayerAnimation
) {
    fun update(deltaTime: Float, input: Vector2) {
        movement.move(input, stamina.isTired())
        stamina.update(deltaTime, movement.isWalking(), movement.isRunning())
        animation.updateAnimationState(movement.getCurrentState())
    }
}
