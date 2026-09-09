package com.islandexplorer.inventory

import com.islandexplorer.environment.DayNightSystem

class TentSystem(private val dayNightSystem: DayNightSystem) {
    fun deployTent() {
        // Deploy tent mesh in front of player
    }
    
    fun rest() {
        // Advance time by 6 hours
        dayNightSystem.advanceTime(6f)
    }
}
