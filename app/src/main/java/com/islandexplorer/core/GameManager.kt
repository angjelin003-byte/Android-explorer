package com.islandexplorer.core

import com.islandexplorer.player.*
import com.islandexplorer.camera.ThirdPersonCamera
import com.islandexplorer.camera.GameCamera
import com.islandexplorer.camera.ViewTransitionManager
import com.islandexplorer.camera.TouchToWorldRaycaster
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
    val gameCamera = GameCamera()
    val viewTransitionManager = ViewTransitionManager()
    val raycaster = TouchToWorldRaycaster()
    val dayNightSystem = DayNightSystem()
    val weatherSystem = WeatherSystem()
    val terrainManager = TerrainManager()
    val torchSystem = TorchSystem()
    val tentSystem = TentSystem(dayNightSystem)
    
    // Tap marker indicator in world space
    var waypointTarget: Vector3? = null
    var waypointTimer: Float = 0f

    private var lastFrameTime = System.nanoTime()

    fun initGame() {
        println("Initializing Vertical 3D Island Explorer Architecture...")
        movement.position.y = terrainManager.getElevationAt(movement.position.x, movement.position.z)
        gameCamera.target = movement.position.copy()
        gameCamera.updateOrbitPosition()
    }
    
    fun update() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        inputManager.update()
        
        movement.move(
            inputManager.virtualJoystickInput,
            gameCamera.yaw,
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

        // Update 3D perspective / Top-down view transition camera
        viewTransitionManager.update(deltaTime, gameCamera, movement.position)

        if (waypointTimer > 0f) {
            waypointTimer -= deltaTime
            if (waypointTimer <= 0f) {
                waypointTarget = null
            }
        }
    }
}
