package com.islandexplorer.player

class PlayerStamina {
    var currentStamina: Float = 100f
    private val maxStamina: Float = 100f

    fun update(deltaTime: Float, isWalking: Boolean, isRunning: Boolean) {
        when {
            isRunning -> currentStamina -= deltaTime * 15f // Drains fast
            isWalking -> currentStamina += deltaTime * 3f  // Regenerates slowly
            else -> currentStamina += deltaTime * 10f      // Regenerates faster when standing still
        }
        currentStamina = currentStamina.coerceIn(0f, maxStamina)
    }
    
    fun isTired(): Boolean = currentStamina <= 5f
    fun getStaminaPercent(): Float = currentStamina / maxStamina
}
