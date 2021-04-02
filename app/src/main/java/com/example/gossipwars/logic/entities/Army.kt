package com.example.gossipwars.logic.entities

data class Army(
    var size: Int = 100000, var attack: Int = 50,
    var defense: Int = 50
) {
    var sizePerRegion: MutableMap<Int, Int> = mutableMapOf()

    companion object Factory {
        fun initDefaultArmy(region: Region): Army = Army().apply {
            this.sizePerRegion[region.id] = this.size
        }
    }
}
