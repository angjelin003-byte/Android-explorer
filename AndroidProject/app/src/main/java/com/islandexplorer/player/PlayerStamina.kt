package com.islandexplorer.player

class PlayerStamina {
    var currentStamina: Float = 100f
    private val maxStamina: Float = 100f

    fun update(deltaTime: Float, isWalking: Boolean, isRunning: Boolean) {
        if (isRunning) {
            currentStamina -= deltaTime * 10f
        } else if (isWalking) {
            currentStamina += deltaTime * 2f
        } else {
            currentStamina += deltaTime * 5f
        }
        currentStamina = currentStamina.coerceIn(0f, maxStamina)
    }
    fun isTired(): Boolean = currentStamina <= 0f
}
