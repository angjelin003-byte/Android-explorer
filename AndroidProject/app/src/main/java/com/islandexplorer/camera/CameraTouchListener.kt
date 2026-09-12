package com.islandexplorer.camera

import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3

interface CameraTouchListener {
    fun onWorldTargetTapped(worldPoint: Vector3, screenPoint: Vector2)
    fun onViewModeToggled(newMode: CameraViewMode)
}
