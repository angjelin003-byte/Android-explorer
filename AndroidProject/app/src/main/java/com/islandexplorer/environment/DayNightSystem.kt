package com.islandexplorer.environment

class DayNightSystem {
    var timeOfDay: Float = 12.0f // 0 to 24

    fun update(deltaTime: Float) {
        timeOfDay += deltaTime * 0.01f
        if (timeOfDay >= 24f) timeOfDay = 0f
        // Update directional light and skybox colors
    }
    
    fun advanceTime(hours: Float) {
        timeOfDay = (timeOfDay + hours) % 24f
    }
}
