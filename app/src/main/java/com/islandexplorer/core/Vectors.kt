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
