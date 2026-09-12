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
