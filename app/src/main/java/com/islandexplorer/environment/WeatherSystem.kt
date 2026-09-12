package com.islandexplorer.environment

enum class WeatherState(val fogDensity: Float, val r: Int, val g: Int, val b: Int) { 
    CLEAR(0.0f, 255, 255, 255), 
    HAZE(0.3f, 200, 210, 220), 
    FOG(0.7f, 160, 170, 180), 
    DENSE_FOG(0.95f, 120, 130, 140), 
    MIST(0.4f, 180, 190, 200) 
}

class WeatherSystem {
    var currentWeather: WeatherState = WeatherState.FOG
        private set
        
    private var timeUntilNextChange = 300f 

    fun update(deltaTime: Float) {
        timeUntilNextChange -= deltaTime
        if (timeUntilNextChange <= 0f) {
            changeWeather()
            timeUntilNextChange = (300..900).random().toFloat()
        }
    }
    
    private fun changeWeather() {
        val weatherValues = WeatherState.values()
        currentWeather = weatherValues.random()
    }
}
