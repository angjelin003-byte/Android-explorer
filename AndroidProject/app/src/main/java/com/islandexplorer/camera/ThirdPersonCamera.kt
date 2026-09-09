package com.islandexplorer.camera

import kotlin.math.cos
import kotlin.math.sin
import com.islandexplorer.core.Vector3
import com.islandexplorer.core.Vector2

class ThirdPersonCamera {
    var position = Vector3(0f, 5f, -10f)
    var lookAtTarget = Vector3(0f, 0f, 0f)
    
    private var yaw = 0f
    private var pitch = 30f
    private var distance = 5.0f
    private val followSmoothness = 10f
    
    fun update(playerPos: Vector3, inputDelta: Vector2, deltaTime: Float) {
        // Smooth camera rotation based on touch drag
        yaw += inputDelta.x * 0.5f
        pitch -= inputDelta.y * 0.5f
        
        // Clamp pitch so it doesn't go underground or directly overhead
        pitch = pitch.coerceIn(-10f, 80f)
        
        // Calculate desired camera position (orbiting behind player)
        val pitchRad = Math.toRadians(pitch.toDouble())
        val yawRad = Math.toRadians(yaw.toDouble())
        
        val offsetX = (distance * cos(pitchRad) * sin(yawRad)).toFloat()
        val offsetY = (distance * sin(pitchRad)).toFloat()
        val offsetZ = (distance * cos(pitchRad) * cos(yawRad)).toFloat()
        
        val targetPos = Vector3(
            playerPos.x + offsetX,
            playerPos.y + 1.5f + offsetY, // Focus slightly above feet
            playerPos.z + offsetZ
        )
        
        // Slight camera lag/inertia for realism
        position = position.lerp(targetPos, deltaTime * followSmoothness)
        lookAtTarget = playerPos.copy().add(0f, 1.5f, 0f)
        
        // TODO: Implement camera collision raycast here to prevent passing through rocks
    }
}
