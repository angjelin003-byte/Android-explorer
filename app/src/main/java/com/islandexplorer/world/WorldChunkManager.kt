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
