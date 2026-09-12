package com.islandexplorer.inventory

class BackpackSystem {
    var isOpen = false
        private set
        
    private val items = mutableListOf<String>()

    fun toggleBackpack() {
        isOpen = !isOpen
    }

    fun addItem(item: String) {
        if (!items.contains(item)) {
            items.add(item)
        }
    }
    
    fun hasItem(item: String): Boolean = items.contains(item)
}
