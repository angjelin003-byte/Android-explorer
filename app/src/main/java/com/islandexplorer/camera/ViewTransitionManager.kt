package com.islandexplorer.camera

import com.islandexplorer.core.Vector3

enum class CameraViewMode {
    TOP_DOWN,
    PERSPECTIVE_3D
}

class ViewTransitionManager {

    companion object {
        // Top-Down Parameters
        const val TOP_DOWN_PITCH = 86.0f
        const val TOP_DOWN_DISTANCE = 48.0f
        const val TOP_DOWN_FOV = 20.0f

        // 3D Perspective Parameters
        const val PERSPECTIVE_PITCH = 38.0f
        const val PERSPECTIVE_DISTANCE = 18.0f
        const val PERSPECTIVE_FOV = 65.0f
    }

    var currentMode: CameraViewMode = CameraViewMode.PERSPECTIVE_3D
        private set
    var targetMode: CameraViewMode = CameraViewMode.PERSPECTIVE_3D
        private set

    // 0.0f = TOP_DOWN, 1.0f = PERSPECTIVE_3D
    var progress: Float = 1.0f
        private set

    private var transitionDuration: Float = 0.8f
    private var transitionSpeed: Float = 1.0f / transitionDuration
    var isTransitioning: Boolean = false
        private set

    fun toggleViewMode(durationSec: Float = 0.8f) {
        val nextMode = if (targetMode == CameraViewMode.PERSPECTIVE_3D) {
            CameraViewMode.TOP_DOWN
        } else {
            CameraViewMode.PERSPECTIVE_3D
        }
        setViewMode(nextMode, animated = true, durationSec = durationSec)
    }

    fun setViewMode(mode: CameraViewMode, animated: Boolean = true, durationSec: Float = 0.8f) {
        targetMode = mode
        transitionDuration = durationSec.coerceAtLeast(0.05f)
        transitionSpeed = 1.0f / transitionDuration

        if (!animated) {
            progress = if (mode == CameraViewMode.PERSPECTIVE_3D) 1.0f else 0.0f
            currentMode = mode
            isTransitioning = false
        } else {
            isTransitioning = true
        }
    }

    fun update(deltaTime: Float, camera: GameCamera, targetPoint: Vector3) {
        // Interpolate target LookAt smoothly
        camera.target = camera.target.lerp(targetPoint, (deltaTime * 12.0f).coerceIn(0f, 1f))

        if (isTransitioning) {
            val targetProgress = if (targetMode == CameraViewMode.PERSPECTIVE_3D) 1.0f else 0.0f
            val step = transitionSpeed * deltaTime

            progress = if (targetProgress > progress) {
                (progress + step).coerceAtMost(1.0f)
            } else {
                (progress - step).coerceAtLeast(0.0f)
            }

            if (progress == targetProgress) {
                isTransitioning = false
                currentMode = targetMode
            }

            // Smoothstep curve: S(t) = t * t * (3 - 2 * t)
            val smoothT = progress.coerceIn(0f, 1f).let { it * it * (3.0f - 2.0f * it) }

            // Lerp pitch, distance, and fov
            camera.pitch = lerp(TOP_DOWN_PITCH, PERSPECTIVE_PITCH, smoothT)
            camera.distance = lerp(TOP_DOWN_DISTANCE, PERSPECTIVE_DISTANCE, smoothT)
            camera.fov = lerp(TOP_DOWN_FOV, PERSPECTIVE_FOV, smoothT)

            camera.updateOrbitPosition()
        } else {
            // Keep current orientation locked to target while idle
            camera.updateOrbitPosition()
        }
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }
}
