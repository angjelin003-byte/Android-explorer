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
