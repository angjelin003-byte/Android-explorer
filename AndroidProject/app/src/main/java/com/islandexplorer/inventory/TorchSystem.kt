package com.islandexplorer.inventory

class TorchSystem {
    var isEquipped = false
    fun toggleTorch() {
        isEquipped = !isEquipped
        // Enable/disable point/spot light attached to player hand
    }
}
