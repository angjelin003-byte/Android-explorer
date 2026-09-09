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
