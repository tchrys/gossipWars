package com.example.gossipwars.logic.entities

object Game {
    var players : MutableList<Player> = mutableListOf()
    var regions : MutableList<Region> = mutableListOf()
    var alliances : MutableList<Alliance> = mutableListOf()

    init {
        regions = Region.initAllRegions()
    }

    fun acceptPlayer(player : Player) {
        players.add(player)
    }

    fun getKickablePlayers(alliance: Alliance) : List<Player> = alliance.playersInvolved

    fun getJoinablePlayers(alliance: Alliance) : List<Player> =
        players.filter{ player -> !alliance.playersInvolved.contains(player) }

    fun addAlliance(player: Player, name : String) {
        val alliance : Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        player.joinAlliance(alliance)
    }

}