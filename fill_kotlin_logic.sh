#!/bin/bash
BASE_DIR="AndroidProject/app/src/main/java/com/islandexplorer"

# 1. CAMERA & THIRD-PERSON VIEW
cat << 'KOTLIN' > $BASE_DIR/camera/ThirdPersonCamera.kt
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
KOTLIN

# 2 & 3. MOVEMENT SYSTEM & PLAYER CHARACTER
cat << 'KOTLIN' > $BASE_DIR/player/PlayerMovement.kt
package com.islandexplorer.player

import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3

class PlayerMovement {
    var position = Vector3(0f, 0f, 0f)
    var currentSpeed = 0f
    
    private val walkSpeed = 2.5f
    private val runSpeed = 6.0f
    private val acceleration = 5.0f
    private var isWalking = false
    private var isRunning = false
    private var state = PlayerState.IDLE

    fun move(input: Vector2, wantToRun: Boolean, isTired: Boolean, deltaTime: Float) {
        val targetSpeed = if (input.magnitude() > 0.1f) {
            if (wantToRun && !isTired) runSpeed else walkSpeed
        } else {
            0f
        }
        
        // Smooth acceleration/deceleration
        currentSpeed += (targetSpeed - currentSpeed) * acceleration * deltaTime
        
        isWalking = currentSpeed > 0.1f && currentSpeed <= walkSpeed + 0.5f
        isRunning = currentSpeed > walkSpeed + 0.5f
        
        state = when {
            isTired && currentSpeed > 0.1f -> PlayerState.TIRED
            isRunning -> PlayerState.RUNNING
            isWalking -> PlayerState.WALKING
            else -> PlayerState.IDLE
        }
        
        // Apply movement vector relative to camera (simplified)
        if (currentSpeed > 0.1f) {
            position.x += input.x * currentSpeed * deltaTime
            position.z += input.y * currentSpeed * deltaTime
        }
    }
    
    fun isWalking() = isWalking
    fun isRunning() = isRunning
    fun getCurrentState() = state
}
enum class PlayerState { IDLE, WALKING, RUNNING, TIRED, JUMPING, CROUCHING }
KOTLIN

# 4. STAMINA & TIREDNESS
cat << 'KOTLIN' > $BASE_DIR/player/PlayerStamina.kt
package com.islandexplorer.player

class PlayerStamina {
    var currentStamina: Float = 100f
    private val maxStamina: Float = 100f

    fun update(deltaTime: Float, isWalking: Boolean, isRunning: Boolean) {
        when {
            isRunning -> currentStamina -= deltaTime * 15f // Drains fast
            isWalking -> currentStamina += deltaTime * 3f  // Regenerates slowly
            else -> currentStamina += deltaTime * 10f      // Regenerates faster when standing still
        }
        currentStamina = currentStamina.coerceIn(0f, maxStamina)
    }
    
    fun isTired(): Boolean = currentStamina <= 5f
    fun getStaminaPercent(): Float = currentStamina / maxStamina
}
KOTLIN

# 5. ADVANCED TOUCH CONTROLS
cat << 'KOTLIN' > $BASE_DIR/input/TouchInputManager.kt
package com.islandexplorer.input

import com.islandexplorer.core.Vector2

class TouchInputManager {
    var virtualJoystickInput = Vector2(0f, 0f)
    var cameraDragDelta = Vector2(0f, 0f)
    var runButtonPressed = false
    var interactButtonPressed = false
    
    fun update() {
        // Read raw touch pointers here and map them to joystick/camera logic
        // Clear drag delta after reading
        cameraDragDelta = Vector2(0f, 0f)
        interactButtonPressed = false
    }
}
KOTLIN

# 6. BACKPACK SYSTEM (Torch, Tent, Compass, Map)
cat << 'KOTLIN' > $BASE_DIR/inventory/TorchSystem.kt
package com.islandexplorer.inventory

class TorchSystem {
    var isEquipped = false
        private set
        
    var batteryLevel = 100f

    fun toggleTorch() {
        isEquipped = !isEquipped
    }
    
    fun update(deltaTime: Float) {
        if (isEquipped) {
            batteryLevel -= deltaTime * 0.1f // Battery drains very slowly
            batteryLevel = batteryLevel.coerceAtLeast(0f)
            if (batteryLevel <= 0f) isEquipped = false
        }
    }
}
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/inventory/TentSystem.kt
package com.islandexplorer.inventory

import com.islandexplorer.environment.DayNightSystem

class TentSystem(private val dayNightSystem: DayNightSystem) {
    var isDeployed = false
        private set

    fun deployTent() {
        // Condition: Must be in a flat, outdoor location
        isDeployed = true
    }
    
    fun packTent() {
        isDeployed = false
    }
    
    fun restUntilNextShift() {
        if (!isDeployed) return
        
        // Display interaction "Rest until morning?"
        // Advance time by 6 hours smoothly
        dayNightSystem.advanceTime(6f)
    }
}
KOTLIN

# 10. DAY/NIGHT CYCLE
cat << 'KOTLIN' > $BASE_DIR/environment/DayNightSystem.kt
package com.islandexplorer.environment

class DayNightSystem {
    var timeOfDay: Float = 8.0f // Starts at 8:00 AM
        private set

    fun update(deltaTime: Float) {
        // 1 real second = 1 game minute -> deltaTime / 60 hours
        timeOfDay += (deltaTime / 60f) 
        if (timeOfDay >= 24f) timeOfDay -= 24f
    }
    
    fun advanceTime(hoursToAdvance: Float) {
        timeOfDay = (timeOfDay + hoursToAdvance) % 24f
    }
    
    fun getSkyColor(): String {
        return when (timeOfDay) {
            in 5f..7f -> "SUNRISE_COLOR"
            in 7f..17f -> "DAY_COLOR"
            in 17f..19f -> "SUNSET_COLOR"
            else -> "NIGHT_COLOR"
        }
    }
}
KOTLIN

# 23. CORE - Vectors & Architecture Wiring
cat << 'KOTLIN' > $BASE_DIR/core/Vectors.kt
package com.islandexplorer.core

import kotlin.math.sqrt

data class Vector2(var x: Float, var y: Float) {
    fun magnitude() = sqrt(x * x + y * y)
}

data class Vector3(var x: Float, var y: Float, var z: Float) {
    fun add(dx: Float, dy: Float, dz: Float): Vector3 {
        return Vector3(x + dx, y + dy, z + dz)
    }
    fun lerp(target: Vector3, t: Float): Vector3 {
        return Vector3(
            x + (target.x - x) * t,
            y + (target.y - y) * t,
            z + (target.z - z) * t
        )
    }
}
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/core/GameManager.kt
package com.islandexplorer.core

import com.islandexplorer.player.*
import com.islandexplorer.camera.ThirdPersonCamera
import com.islandexplorer.input.TouchInputManager
import com.islandexplorer.environment.DayNightSystem
import com.islandexplorer.inventory.*

class GameManager {
    private val inputManager = TouchInputManager()
    private val movement = PlayerMovement()
    private val stamina = PlayerStamina()
    private val camera = ThirdPersonCamera()
    private val dayNightSystem = DayNightSystem()
    private val torchSystem = TorchSystem()
    private val tentSystem = TentSystem(dayNightSystem)
    
    private var lastFrameTime = System.nanoTime()

    fun initGame() {
        println("Initializing Vertical 3D Island Explorer Architecture...")
    }
    
    fun update() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        // 1. Process Input
        inputManager.update()
        
        // 2. Update Player State
        movement.move(
            inputManager.virtualJoystickInput, 
            inputManager.runButtonPressed, 
            stamina.isTired(), 
            deltaTime
        )
        stamina.update(deltaTime, movement.isWalking(), movement.isRunning())
        
        // 3. Update Equipment & Environment
        torchSystem.update(deltaTime)
        dayNightSystem.update(deltaTime)
        
        // 4. Update Camera (Late update after player has moved)
        camera.update(movement.position, inputManager.cameraDragDelta, deltaTime)
    }
}
KOTLIN

cat << 'KOTLIN' > $BASE_DIR/core/InteractionSystem.kt
package com.islandexplorer.core

class InteractionSystem {
    fun checkForInteractables(playerPosition: Vector3, lookDirection: Vector3) {
        // Raycast forward to see if player is looking at a tent, cave entrance, or item
    }
    
    fun interact(targetId: String) {
        // Trigger interaction event
    }
}
KOTLIN

echo "Kotlin game logic and math filled successfully."
