package com.example.gossipwars.logic.entities

data class Region(val id : Int, val name : String,
                    val neighbors : List<Int>) {
    var occupiedBy: Player? = null

    companion object Factory {
        fun initAllRegions() : MutableList<Region> {
            var result : MutableList<Region> = mutableListOf()
            result.add(Region(1, "England", mutableListOf(2, 4, 5)))
            result.add(Region(2, "France", mutableListOf(1, 3, 4, 5, 6)))
            result.add(Region(3, "Italy", mutableListOf(2, 6, 7)))
            result.add(Region(4, "Germany", mutableListOf(1, 2, 5, 6, 9)))
            result.add(Region(5, "Northern Block", mutableListOf(1, 2, 4, 8)))
            result.add(Region(6, "Central Europe", mutableListOf(2, 3, 4, 7, 9)))
            result.add(Region(7, "Balkans", mutableListOf(3, 5, 6, 9, 10)))
            result.add(Region(8, "Russia", mutableListOf(9, 10)))
            result.add(Region(9, "Eastern Block", mutableListOf(4, 5, 6, 7, 8, 10)))
            result.add(Region(10, "Turkey", mutableListOf(7, 8, 9)))
            return result
        }
    }

}