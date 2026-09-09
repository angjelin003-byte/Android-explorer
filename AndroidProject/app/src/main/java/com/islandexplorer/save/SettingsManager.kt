package com.islandexplorer.save

class SettingsManager {
    var graphicsQuality: Quality = Quality.MEDIUM
    
    fun applySettings() {
        // Apply shadow quality, LOD distance, fog
    }
}
enum class Quality { LOW, MEDIUM, HIGH }
