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
