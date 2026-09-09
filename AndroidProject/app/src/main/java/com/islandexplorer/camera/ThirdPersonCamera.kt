package com.islandexplorer.camera

class ThirdPersonCamera {
    fun updateCameraPosition(playerPos: Vector3, inputDelta: Vector2) {
        // Smoothly follow player
        // Apply rotation from touch input
        // Handle collision with terrain (don't clip through trees/rocks)
    }
}
class Vector3(val x: Float, val y: Float, val z: Float)
