package com.islandexplorer.core

import com.islandexplorer.player.*
import com.islandexplorer.camera.ThirdPersonCamera
import com.islandexplorer.input.TouchInputManager
import com.islandexplorer.environment.DayNightSystem
import com.islandexplorer.environment.WeatherSystem
import com.islandexplorer.inventory.*
import com.islandexplorer.world.TerrainManager

class GameManager {
    val inputManager = TouchInputManager()
    val movement = PlayerMovement()
    val stamina = PlayerStamina()
    val camera = ThirdPersonCamera()
    val dayNightSystem = DayNightSystem()
    val weatherSystem = WeatherSystem()
    val terrainManager = TerrainManager()
    val torchSystem = TorchSystem()
    val tentSystem = TentSystem(dayNightSystem)
    
    private var lastFrameTime = System.nanoTime()

    fun initGame() {
        println("Initializing Vertical 3D Island Explorer Architecture...")
        movement.position.y = terrainManager.getElevationAt(movement.position.x, movement.position.z)
    }
    
    fun update() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        inputManager.update()
        
        movement.move(
            inputManager.virtualJoystickInput, 
            inputManager.runButtonPressed, 
            stamina.isTired(), 
            deltaTime
        )
        
        // SNAP TO TERRAIN: Player always walks ON the terrain elevation mathematically
        val targetY = terrainManager.getElevationAt(movement.position.x, movement.position.z)
        // Lerp Y for smooth stepping up/down hills
        movement.position.y += (targetY - movement.position.y) * 10f * deltaTime
        
        stamina.update(deltaTime, movement.isWalking(), movement.isRunning())
        torchSystem.update(deltaTime)
        dayNightSystem.update(deltaTime)
        weatherSystem.update(deltaTime)
        camera.update(movement.position, inputManager.cameraDragDelta, deltaTime)
    }
}
