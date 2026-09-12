package com.islandexplorer.camera

import android.opengl.Matrix
import com.islandexplorer.core.Vector2
import com.islandexplorer.core.Vector3
import kotlin.math.cos
import kotlin.math.sin

class GameCamera {
    var position = Vector3(0f, 15f, -15f)
    var target = Vector3(0f, 0f, 0f)
    val up = Vector3(0f, 1f, 0f)

    var fov: Float = 65.0f
    var pitch: Float = 38.0f // degrees, 0 = horizontal, 90 = straight down
    var yaw: Float = 0.0f   // degrees around Y axis
    var distance: Float = 18.0f

    var nearClip: Float = 0.5f
    var farClip: Float = 600.0f

    // 4x4 Transformation Matrices
    val viewMatrix = FloatArray(16)
    val projMatrix = FloatArray(16)
    val viewProjMatrix = FloatArray(16)
    val invViewProjMatrix = FloatArray(16)

    // Temporary buffers for projection calculations to avoid allocations
    private val worldPointBuf = FloatArray(4)
    private val clipPointBuf = FloatArray(4)

    init {
        updateOrbitPosition()
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projMatrix, 0)
        Matrix.setIdentityM(viewProjMatrix, 0)
        Matrix.setIdentityM(invViewProjMatrix, 0)
    }

    /**
     * Recalculates position based on target, distance, pitch, and yaw.
     */
    fun updateOrbitPosition() {
        val clampedPitch = pitch.coerceIn(15.0f, 88.0f)
        val pitchRad = Math.toRadians(clampedPitch.toDouble())
        val yawRad = Math.toRadians(yaw.toDouble())

        val offsetX = (distance * cos(pitchRad) * sin(yawRad)).toFloat()
        val offsetY = (distance * sin(pitchRad)).toFloat()
        val offsetZ = (distance * cos(pitchRad) * cos(yawRad)).toFloat()

        position.x = target.x + offsetX
        position.y = target.y + offsetY
        position.z = target.z + offsetZ
    }

    /**
     * Updates View, Projection, ViewProjection and Inverse ViewProjection matrices.
     */
    fun updateMatrices(viewportWidth: Float, viewportHeight: Float) {
        val aspect = if (viewportHeight > 0f) viewportWidth / viewportHeight else 1.0f

        // 1. View Matrix: Camera looking at Target
        Matrix.setLookAtM(
            viewMatrix, 0,
            position.x, position.y, position.z,
            target.x, target.y, target.z,
            up.x, up.y, up.z
        )

        // 2. Perspective Projection Matrix
        val safeFov = fov.coerceIn(10.0f, 90.0f)
        Matrix.perspectiveM(
            projMatrix, 0,
            safeFov,
            aspect,
            nearClip,
            farClip
        )

        // 3. Combined View-Projection Matrix = P * V
        Matrix.multiplyMM(viewProjMatrix, 0, projMatrix, 0, viewMatrix, 0)

        // 4. Inverse View-Projection Matrix = (P * V)^(-1) for raycasting
        Matrix.invertM(invViewProjMatrix, 0, viewProjMatrix, 0)
    }

    /**
     * Projects a 3D world coordinate into 2D screen coordinate.
     * Returns true if point is in front of the camera and successfully mapped.
     */
    fun worldToScreen(
        worldX: Float, worldY: Float, worldZ: Float,
        screenWidth: Float, screenHeight: Float,
        outScreen: Vector2
    ): Boolean {
        worldPointBuf[0] = worldX
        worldPointBuf[1] = worldY
        worldPointBuf[2] = worldZ
        worldPointBuf[3] = 1.0f

        // Transform into clip space: clip = ViewProj * world
        Matrix.multiplyMV(clipPointBuf, 0, viewProjMatrix, 0, worldPointBuf, 0)

        val w = clipPointBuf[3]
        if (w <= 0.001f) {
            return false // Point is behind near clipping plane
        }

        // Perspective divide to NDC space [-1, 1]
        val ndcX = clipPointBuf[0] / w
        val ndcY = clipPointBuf[1] / w

        // Map NDC [-1, 1] to screen space [0, width] x [0, height]
        outScreen.x = (ndcX + 1.0f) * 0.5f * screenWidth
        outScreen.y = (1.0f - ndcY) * 0.5f * screenHeight
        return true
    }

    fun projectPoint(worldPos: Vector3, screenWidth: Float, screenHeight: Float): Vector2? {
        val out = Vector2(0f, 0f)
        return if (worldToScreen(worldPos.x, worldPos.y, worldPos.z, screenWidth, screenHeight, out)) {
            out
        } else {
            null
        }
    }
}
