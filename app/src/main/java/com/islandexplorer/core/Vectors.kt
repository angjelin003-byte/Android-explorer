package com.islandexplorer.core

import kotlin.math.sqrt

data class Vector2(var x: Float, var y: Float) {
    fun magnitude() = sqrt(x * x + y * y)
    fun distanceTo(other: Vector2): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }
}

data class Vector3(var x: Float, var y: Float, var z: Float) {
    fun add(dx: Float, dy: Float, dz: Float): Vector3 {
        return Vector3(x + dx, y + dy, z + dz)
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(scalar: Float): Vector3 {
        return Vector3(x * scalar, y * scalar, z * scalar)
    }

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun distanceTo(other: Vector3): Float {
        val dx = other.x - x
        val dy = other.y - y
        val dz = other.z - z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.00001f) Vector3(x / len, y / len, z / len) else Vector3(0f, 0f, 0f)
    }

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 {
        return Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun lerp(target: Vector3, t: Float): Vector3 {
        val clampedT = t.coerceIn(0f, 1f)
        return Vector3(
            x + (target.x - x) * clampedT,
            y + (target.y - y) * clampedT,
            z + (target.z - z) * clampedT
        )
    }

    companion object {
        fun lerp(a: Vector3, b: Vector3, t: Float): Vector3 {
            return a.lerp(b, t)
        }
    }
}
