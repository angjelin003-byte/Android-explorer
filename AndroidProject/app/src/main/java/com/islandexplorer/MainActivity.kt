package com.islandexplorer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.islandexplorer.core.GameManager

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup SurfaceView for 3D rendering
        
        gameManager = GameManager()
        gameManager.initGame()
    }
}
