package com.islandexplorer.environment

enum class WeatherState { CLEAR, HAZE, FOG, DENSE_FOG, MIST }

class WeatherSystem {
    var currentWeather: WeatherState = WeatherState.CLEAR
        private set
        
    private var timeUntilNextChange = 300f // 5 minutes

    fun update(deltaTime: Float) {
        timeUntilNextChange -= deltaTime
        if (timeUntilNextChange <= 0f) {
            changeWeather()
            timeUntilNextChange = (300f..900f).random() // 5 to 15 minutes
        }
    }
    
    private fun changeWeather() {
        val weatherValues = WeatherState.values()
        currentWeather = weatherValues.random()
        // Interpolate fog density and skybox based on new weather
    }
}
