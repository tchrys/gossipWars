package com.example.gossipwars.logic.entities

data class Region(
    val id: Int, val name: String,
    val neighbors: List<Int>
) {
    var occupiedBy: Player? = null

    companion object Factory {
        fun initAllRegions(): MutableList<Region> = mutableListOf(
            Region(1, "BadenWurttemberg", mutableListOf(2, 4, 8)),
            Region(2, "Bavaria", mutableListOf(1, 4, 10, 13)),
            Region(3, "Brandenburg", mutableListOf(5, 10, 11)),
            Region(4, "Hesse", mutableListOf(1, 2, 6, 7, 8, 13)),
            Region(5, "Mecklenburg", mutableListOf(3, 6, 12)),
            Region(6, "LowerSaxony", mutableListOf(4, 5, 7, 11, 12, 13)),
            Region(7, "NorthRhineWestphalia", mutableListOf(4, 6, 8)),
            Region(8, "Rhineland", mutableListOf(1, 4, 7, 9)),
            Region(9, "Saarland", mutableListOf(8)),
            Region(10, "Saxony", mutableListOf(2, 3, 11, 13)),
            Region(11, "SaxonyAnhalt", mutableListOf(3, 6, 10, 13)),
            Region(12, "SchleswigHolstein", mutableListOf(5, 6)),
            Region(13, "Thuringia", mutableListOf(2, 4, 6, 10, 11))
        )
    }

    fun getNeighborsList(): List<Region> =
        Game.regions.filter { region -> this.neighbors.contains(region.id) }

}