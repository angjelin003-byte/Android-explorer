package com.islandexplorer.camera

import android.opengl.Matrix
import com.islandexplorer.core.Vector3
import com.islandexplorer.world.TerrainManager
import kotlin.math.abs

data class Ray3D(
    val origin: Vector3,
    val direction: Vector3
) {
    fun getPointAt(t: Float): Vector3 {
        return Vector3(
            origin.x + direction.x * t,
            origin.y + direction.y * t,
            origin.z + direction.z * t
        )
    }
}

class TouchToWorldRaycaster {

    private val nearClipBuf = FloatArray(4)
    private val farClipBuf = FloatArray(4)
    private val nearWorldBuf = FloatArray(4)
    private val farWorldBuf = FloatArray(4)

    /**
     * Converts 2D screen coordinates into a normalized 3D ray in world space.
     * Uses the inverted View-Projection matrix: D_world = (P * V)^(-1) * V_ndc.
     */
    fun screenToWorldRay(
        screenX: Float,
        screenY: Float,
        screenWidth: Float,
        screenHeight: Float,
        camera: GameCamera
    ): Ray3D {
        if (screenWidth <= 0f || screenHeight <= 0f) {
            return Ray3D(camera.position, Vector3(0f, -1f, 0f))
        }

        // 1. Normalized Device Coordinates (NDC) in [-1, 1] range
        val ndcX = (2.0f * screenX) / screenWidth - 1.0f
        val ndcY = 1.0f - (2.0f * screenY) / screenHeight

        // Near plane point in NDC (z = -1.0)
        nearClipBuf[0] = ndcX
        nearClipBuf[1] = ndcY
        nearClipBuf[2] = -1.0f
        nearClipBuf[3] = 1.0f

        // Far plane point in NDC (z = 1.0)
        farClipBuf[0] = ndcX
        farClipBuf[1] = ndcY
        farClipBuf[2] = 1.0f
        farClipBuf[3] = 1.0f

        // 2. Unproject using inverse View-Projection Matrix: (P * V)^(-1)
        Matrix.multiplyMV(nearWorldBuf, 0, camera.invViewProjMatrix, 0, nearClipBuf, 0)
        Matrix.multiplyMV(farWorldBuf, 0, camera.invViewProjMatrix, 0, farClipBuf, 0)

        // 3. Perspective divide
        val nearW = if (nearWorldBuf[3] != 0f) nearWorldBuf[3] else 1.0f
        val farW = if (farWorldBuf[3] != 0f) farWorldBuf[3] else 1.0f

        val nearX = nearWorldBuf[0] / nearW
        val nearY = nearWorldBuf[1] / nearW
        val nearZ = nearWorldBuf[2] / nearW

        val farX = farWorldBuf[0] / farW
        val farY = farWorldBuf[1] / farW
        val farZ = farWorldBuf[2] / farW

        val rayOrigin = Vector3(nearX, nearY, nearZ)
        val dir = Vector3(farX - nearX, farY - nearY, farZ - nearZ).normalized()

        return Ray3D(rayOrigin, dir)
    }

    /**
     * Intersects screen touch with an arbitrary horizontal ground plane (default Y = 0).
     */
    fun raycastGroundPlane(
        screenX: Float,
        screenY: Float,
        screenWidth: Float,
        screenHeight: Float,
        camera: GameCamera,
        planeY: Float = 0.0f
    ): Vector3? {
        val ray = screenToWorldRay(screenX, screenY, screenWidth, screenHeight, camera)

        // Prevent divide by near-zero if ray is parallel to the plane
        if (abs(ray.direction.y) < 0.0001f) {
            return null
        }

        // P.y = O.y + t * D.y = planeY  =>  t = (planeY - O.y) / D.y
        val t = (planeY - ray.origin.y) / ray.direction.y
        if (t < 0.0f) {
            return null // Intersection is behind camera
        }

        return ray.getPointAt(t)
    }

    /**
     * Intersects screen touch with the 3D procedural terrain elevation heightmap.
     * Uses ray-marching with binary refinement for precise ground hit detection.
     */
    fun raycastTerrain(
        screenX: Float,
        screenY: Float,
        screenWidth: Float,
        screenHeight: Float,
        camera: GameCamera,
        terrainManager: TerrainManager
    ): Vector3? {
        val ray = screenToWorldRay(screenX, screenY, screenWidth, screenHeight, camera)

        var t = 1.0f
        val maxDist = 300.0f
        val step = 1.5f
        var prevPoint = ray.getPointAt(t)
        var hitFound = false

        while (t < maxDist) {
            val currPoint = ray.getPointAt(t)
            val terrainHeight = terrainManager.getElevationAt(currPoint.x, currPoint.z)

            if (currPoint.y <= terrainHeight) {
                hitFound = true
                // Binary refinement between prevPoint and currPoint
                var tLow = t - step
                var tHigh = t
                for (iter in 0..6) {
                    val tMid = (tLow + tHigh) * 0.5f
                    val midPoint = ray.getPointAt(tMid)
                    val midElevation = terrainManager.getElevationAt(midPoint.x, midPoint.z)
                    if (midPoint.y <= midElevation) {
                        tHigh = tMid
                    } else {
                        tLow = tMid
                    }
                }
                val finalHit = ray.getPointAt(tHigh)
                finalHit.y = terrainManager.getElevationAt(finalHit.x, finalHit.z)
                return finalHit
            }

            prevPoint = currPoint
            t += step
        }

        // Fallback to ground plane (Y=0) if ray marched out of height range
        return raycastGroundPlane(screenX, screenY, screenWidth, screenHeight, camera, 0.0f)
    }
}
