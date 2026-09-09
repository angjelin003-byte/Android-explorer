package com.islandexplorer.inventory

class TorchSystem {
    var isEquipped = false
        private set
        
    var batteryLevel = 100f

    fun toggleTorch() {
        isEquipped = !isEquipped
    }
    
    fun update(deltaTime: Float) {
        if (isEquipped) {
            batteryLevel -= deltaTime * 0.1f // Battery drains very slowly
            batteryLevel = batteryLevel.coerceAtLeast(0f)
            if (batteryLevel <= 0f) isEquipped = false
        }
    }
}
