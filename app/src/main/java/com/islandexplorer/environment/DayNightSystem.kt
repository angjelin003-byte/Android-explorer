package com.islandexplorer.environment

class DayNightSystem {
    var timeOfDay: Float = 8.0f // Starts at 8:00 AM
        private set

    fun update(deltaTime: Float) {
        // 1 real second = 1 game minute -> deltaTime / 60 hours
        timeOfDay += (deltaTime / 60f) 
        if (timeOfDay >= 24f) timeOfDay -= 24f
    }
    
    fun advanceTime(hoursToAdvance: Float) {
        timeOfDay = (timeOfDay + hoursToAdvance) % 24f
    }
    
    fun getSkyColor(): String {
        return when (timeOfDay) {
            in 5f..7f -> "SUNRISE_COLOR"
            in 7f..17f -> "DAY_COLOR"
            in 17f..19f -> "SUNSET_COLOR"
            else -> "NIGHT_COLOR"
        }
    }
}
