package com.islandexplorer.inventory

class CompassSystem {
    fun getHeading(playerYawRotation: Float): String {
        // Normalize angle to 0-360
        val normalized = ((playerYawRotation % 360f) + 360f) % 360f
        return when {
            normalized >= 337.5f || normalized < 22.5f -> "N"
            normalized in 22.5f..67.5f -> "NE"
            normalized in 67.5f..112.5f -> "E"
            normalized in 112.5f..157.5f -> "SE"
            normalized in 157.5f..202.5f -> "S"
            normalized in 202.5f..247.5f -> "SW"
            normalized in 247.5f..292.5f -> "W"
            normalized in 292.5f..337.5f -> "NW"
            else -> "N"
        }
    }
}
