package com.example.gossipwars.logic.entities

data class Army(var size : Int = 100000, var attack : Double = 50.0,
                var defense : Double = 50.0) {
    var sizePerRegion : MutableMap<Int, Int> = mutableMapOf()

    companion object Factory {
        fun initDefaultArmy(region: Region) : Army {
            var army = Army()
            army.sizePerRegion[region.id] = army.size
            return army
        }
    }

}