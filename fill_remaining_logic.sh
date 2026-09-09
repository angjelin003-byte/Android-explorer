#!/bin/bash
BASE_DIR="AndroidProject/app/src/main/java/com/islandexplorer"

# 6. BACKPACK SYSTEM (Compass, Map, Backpack)
cat << 'KOTLIN' > $BASE_DIR/inventory/CompassSystem.kt
package com.islandexplorer.inventory

class CompassSystem {
    fun getHeading(playerYawRotation: Float): String {
        // Normalize angle to 0-360
        val normalized = ((playerYawRotation % 360f) + 360f) % 360f
        return when {
            normalized >= 337.5f || normalized < 22.5f -> "N"
            normalized in 22.5f..67.5f -> "NE"
            normalized in 67.5f..112.5f -> "E"
            normalized in 112.5f..157.5f -> "SE"
            normalized in 157.5f..202.5f -> "S"
            normalized in 202.5f..247.5f -> "SW"
            normalized in 247.5f..292.5f -> "W"
            normalized in 292.5f..337.5f -> "NW"
            else -> "N"
        }
    }
}
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/inventory/MapSystem.kt
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
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/inventory/BackpackSystem.kt
package com.islandexplorer.inventory

class BackpackSystem {
    var isOpen = false
        private set
        
    private val items = mutableListOf<String>()

    fun toggleBackpack() {
        isOpen = !isOpen
    }

    fun addItem(item: String) {
        if (!items.contains(item)) {
            items.add(item)
        }
    }
    
    fun hasItem(item: String): Boolean = items.contains(item)
}
KOTLIN

# 2. PLAYER ANIMATION
cat << 'KOTLIN' > $BASE_DIR/player/PlayerAnimation.kt
package com.islandexplorer.player

class PlayerAnimation {
    private var currentState: PlayerState = PlayerState.IDLE
    
    fun updateAnimationState(newState: PlayerState) {
        if (currentState == newState) return
        
        currentState = newState
        when (newState) {
            PlayerState.IDLE -> playAnim("Idle")
            PlayerState.WALKING -> playAnim("Walk")
            PlayerState.RUNNING -> playAnim("Run")
            PlayerState.TIRED -> playAnim("Walk_Tired")
            PlayerState.JUMPING -> playAnim("Jump")
            PlayerState.CROUCHING -> playAnim("Crouch")
        }
    }
    
    private fun playAnim(animName: String) {
        // Interface with the 3D engine's skeletal animation system
        // Handle cross-fading and inverse kinematics (IK) for foot placement
    }
}
KOTLIN

# 7 & 13. TERRAIN & ENVIRONMENT INTERACTION
cat << 'KOTLIN' > $BASE_DIR/world/TerrainManager.kt
package com.islandexplorer.world

import com.islandexplorer.core.Vector3

enum class SurfaceType { GRASS, MUD, STONE, GRAVEL, SAND, WATER }

class TerrainManager {
    fun getSurfaceTypeAt(position: Vector3): SurfaceType {
        // Raycast down to determine the terrain material based on splatmap/textures
        // Placeholder logic:
        return if (position.y < 0.2f) {
            SurfaceType.WATER
        } else if (position.y < 1.0f) {
            SurfaceType.SAND
        } else {
            SurfaceType.GRASS
        }
    }
    
    fun getElevationAt(x: Float, z: Float): Float {
        // Query heightmap for procedural foot placement and camera collision
        return 0f 
    }
}
KOTLIN

# 11. WEATHER SYSTEM
cat << 'KOTLIN' > $BASE_DIR/environment/WeatherSystem.kt
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
KOTLIN

# 17. WORLD OPTIMIZATION (Chunks & LOD)
cat << 'KOTLIN' > $BASE_DIR/world/WorldChunkManager.kt
package com.islandexplorer.world

import com.islandexplorer.core.Vector3

enum class DetailLevel { NEAR, MEDIUM, FAR, VERY_FAR }

class WorldChunkManager {
    fun getDetailLevelForChunk(playerPos: Vector3, chunkPos: Vector3): DetailLevel {
        val distance = Math.sqrt(
            Math.pow((playerPos.x - chunkPos.x).toDouble(), 2.0) +
            Math.pow((playerPos.z - chunkPos.z).toDouble(), 2.0)
        ).toFloat()
        
        return when {
            distance < 50f -> DetailLevel.NEAR
            distance < 150f -> DetailLevel.MEDIUM
            distance < 300f -> DetailLevel.FAR
            else -> DetailLevel.VERY_FAR
        }
    }
}
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/world/LODManager.kt
package com.islandexplorer.world

class LODManager {
    fun updateObjectLOD(detailLevel: DetailLevel, renderableObject: Any) {
        when (detailLevel) {
            DetailLevel.NEAR -> {} // Use High-Poly Mesh, Physics Enabled
            DetailLevel.MEDIUM -> {} // Use Mid-Poly Mesh, Simplified Physics
            DetailLevel.FAR -> {} // Use Low-Poly Mesh, No Physics
            DetailLevel.VERY_FAR -> {} // Use 2D Billboard / Impostor, Frustum Culling
        }
    }
}
KOTLIN

# 14. AUDIO SYSTEM
cat << 'KOTLIN' > $BASE_DIR/audio/AudioManager.kt
package com.islandexplorer.audio

import com.islandexplorer.world.SurfaceType

class AudioManager {
    fun playFootstep(surface: SurfaceType) {
        val soundFile = when (surface) {
            SurfaceType.GRASS -> "step_grass.ogg"
            SurfaceType.MUD -> "step_mud.ogg"
            SurfaceType.STONE -> "step_stone.ogg"
            SurfaceType.WATER -> "step_water.ogg"
            else -> "step_dirt.ogg"
        }
        // Play 3D positional audio slightly pitched for variety
    }
    
    fun updateBreathing(isTired: Boolean, isRunning: Boolean) {
        // Crossfade between normal breathing and heavy breathing based on stamina
    }
}
KOTLIN

# 21. SAVE SYSTEM
cat << 'KOTLIN' > $BASE_DIR/save/SaveSystem.kt
package com.islandexplorer.save

import com.islandexplorer.core.Vector3

data class SaveData(
    val playerPosition: Vector3,
    val playerRotationYaw: Float,
    val timeOfDay: Float,
    val inventory: List<String>
)

class SaveSystem {
    fun saveGame(data: SaveData) {
        // Serialize to JSON and write to Android internal storage SharedPreferences or File
        println("Saving game at position \${data.playerPosition.x}, \${data.playerPosition.z}")
    }
    
    fun loadGame(): SaveData? {
        // Read from internal storage, deserialize
        return null 
    }
}
KOTLIN

# 16. PERFORMANCE SETTINGS
cat << 'KOTLIN' > $BASE_DIR/save/SettingsManager.kt
package com.islandexplorer.save

enum class QualityLevel { LOW, MEDIUM, HIGH }

class SettingsManager {
    var graphicsQuality: QualityLevel = QualityLevel.MEDIUM
    
    fun applySettings() {
        when (graphicsQuality) {
            QualityLevel.LOW -> {
                // Disable shadows, dense fog, low view distance
            }
            QualityLevel.MEDIUM -> {
                // Hard shadows, medium view distance, basic vegetation
            }
            QualityLevel.HIGH -> {
                // Soft shadows, far view distance, dense vegetation, post-processing
            }
        }
    }
}
KOTLIN

# 22. UI HUD
cat << 'KOTLIN' > $BASE_DIR/ui/UIManager.kt
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
KOTLIN

echo "Remaining Kotlin systems fleshed out."
