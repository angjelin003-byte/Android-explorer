package com.islandexplorer.world

import com.islandexplorer.core.Vector3
import kotlin.math.sin
import kotlin.math.cos

enum class SurfaceType { GRASS, MUD, STONE, GRAVEL, SAND, WATER }

class TerrainManager {
    fun getSurfaceTypeAt(position: Vector3): SurfaceType {
        val elevation = getElevationAt(position.x, position.z)
        return when {
            elevation < -1.0f -> SurfaceType.WATER
            elevation < 0.5f -> SurfaceType.SAND
            elevation < 6.0f -> SurfaceType.GRASS
            elevation < 9.0f -> SurfaceType.MUD
            else -> SurfaceType.STONE
        }
    }
    
    fun getElevationAt(x: Float, z: Float): Float {
        // Procedural heightmap: Combine multiple frequencies of sine/cosine waves for realistic hills and valleys
        val frequency1 = 0.05f
        val frequency2 = 0.01f
        
        val detail = sin(x * frequency1) * cos(z * frequency1) * 2f
        val hills = sin(x * frequency2) * cos(z * frequency2) * 12f
        
        return detail + hills
    }
}
