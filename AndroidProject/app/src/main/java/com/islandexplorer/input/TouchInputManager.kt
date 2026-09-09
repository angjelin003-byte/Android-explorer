package com.islandexplorer.input

class TouchInputManager {
    fun getMovementInput(): Vector2 {
        // Parse left side virtual joystick
        return Vector2(0f, 0f)
    }
    fun getLookInput(): Vector2 {
        // Parse right side drag for camera
        return Vector2(0f, 0f)
    }
}
class Vector2(val x: Float, val y: Float)
