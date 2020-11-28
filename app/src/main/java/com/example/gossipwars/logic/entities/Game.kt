package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.RoomInfo
import java.util.*

object Game {
    var players : MutableList<Player> = mutableListOf()
    var regions : MutableList<Region> = mutableListOf()
    var alliances : MutableList<Alliance> = mutableListOf()
    var regionsPerPlayers : MutableMap<Int, UUID> = mutableMapOf()
    val noOfRegions : Int = 10;
    var noOfRounds : Int = 0;
    var roomInfo : RoomInfo? = null

    init {
        regions = Region.initAllRegions()
    }

    fun initRegionForPlayer(playerId : UUID) : Region {
        var idxInList = players.indexOfFirst { player -> player.id == playerId }
        if (idxInList == -1)
            idxInList = 0
        regionsPerPlayers[idxInList] = playerId
        return regions[idxInList]
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