package com.islandexplorer.environment

class DayNightSystem {
    var timeOfDay: Float = 8.0f 
    var timeScale: Float = 1.0f 
    
    fun update(deltaTime: Float) {
        timeOfDay += (deltaTime / 60.0f) * timeScale
        if (timeOfDay >= 24.0f) {
            timeOfDay = 0.0f
        }
    }

    fun setTime(time: Float) {
        timeOfDay = time.coerceIn(0.0f, 24.0f)
    }

    fun advanceTime(hours: Float) {
        timeOfDay = (timeOfDay + hours) % 24.0f
    }
    
    fun getAmbientLight(): Float {
        return when {
            timeOfDay in 6.0f..18.0f -> 1.0f 
            timeOfDay in 18.0f..20.0f -> 1.0f - ((timeOfDay - 18.0f) / 2.0f) * 0.8f 
            timeOfDay in 4.0f..6.0f -> 0.2f + ((timeOfDay - 4.0f) / 2.0f) * 0.8f 
            else -> 0.2f 
        }
    }
}
