package com.islandexplorer.ui

class UIManager {
    fun updateStaminaHUD(percent: Float) {
        // Adjust stamina bar width/color based on percent
        // Hide if at 100% to keep UI minimal
    }
    
    fun updateCompassHUD(heading: String) {
        // Show N, S, E, W
    }
    
    fun showInteractionPrompt(text: String) {
        // e.g. "Rest until morning?"
    }
    
    fun hideInteractionPrompt() {
        // Clear prompt
    }
}
