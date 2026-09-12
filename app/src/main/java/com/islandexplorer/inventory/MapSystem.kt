package com.islandexplorer.inventory

import com.islandexplorer.core.Vector3
import kotlin.math.floor

class MapSystem {
    // 1km x 1km island represented in 10m chunks (100x100 grid)
    private val gridSize = 100
    private val chunkSize = 10f
    private val exploredMap = BooleanArray(gridSize * gridSize) { false }

    fun updateExploration(playerPosition: Vector3) {
        val gridX = floor((playerPosition.x + 500f) / chunkSize).toInt().coerceIn(0, gridSize - 1)
        val gridY = floor((playerPosition.z + 500f) / chunkSize).toInt().coerceIn(0, gridSize - 1)
        
        val index = gridY * gridSize + gridX
        if (!exploredMap[index]) {
            exploredMap[index] = true
            // Trigger UI update or discovery event
        }
    }
    
    fun isExplored(worldX: Float, worldZ: Float): Boolean {
        val gridX = floor((worldX + 500f) / chunkSize).toInt().coerceIn(0, gridSize - 1)
        val gridY = floor((worldZ + 500f) / chunkSize).toInt().coerceIn(0, gridSize - 1)
        return exploredMap[gridY * gridSize + gridX]
    }
}
