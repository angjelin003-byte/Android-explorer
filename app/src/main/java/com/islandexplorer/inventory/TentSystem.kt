package com.islandexplorer.inventory

import com.islandexplorer.environment.DayNightSystem

class TentSystem(private val dayNightSystem: DayNightSystem) {
    var isDeployed = false
        private set

    fun deployTent() {
        // Condition: Must be in a flat, outdoor location
        isDeployed = true
    }
    
    fun packTent() {
        isDeployed = false
    }
    
    fun restUntilNextShift() {
        if (!isDeployed) return
        
        // Display interaction "Rest until morning?"
        // Advance time by 6 hours smoothly
        dayNightSystem.advanceTime(6f)
    }
}
