package com.example.gossipwars.logic.entities

import java.util.*

data class Player(var username : String, val id : UUID) {
    var army : Army? = null
    var regionsOccupied : MutableSet<Region> = mutableSetOf()
    var trustInOthers : MutableMap<UUID, Int> = mutableMapOf()
    var alliances : MutableSet<Alliance> = mutableSetOf()

    companion object Factory {
        fun initPlayer(username: String, region: Region) : Player {
            var player : Player = Player(username, UUID.randomUUID())
            player.winRegion(region)
            player.army = Army.initDefaultArmy(region)
            return player
        }
    }

    fun quitAlliance(alliance: Alliance) {
        alliances.remove(alliance)
    }

    fun joinAlliance(alliance: Alliance) {
        alliances.add(alliance)
    }

    fun winRegion(region: Region) {
        regionsOccupied.add(region)
        region.occupiedBy = this
    }

    fun loseRegion(region: Region) {
        regionsOccupied.remove(region)
    }

    fun moveTroops(fromRegion : Int, toRegion : Int, size : Int) {
        if (!army?.sizePerRegion?.containsKey(fromRegion)!! || !army?.sizePerRegion?.containsKey(toRegion)!!)
            return;
        if (size > army?.sizePerRegion?.get(fromRegion)!!)
            return
        army?.sizePerRegion?.put(fromRegion, army?.sizePerRegion?.get(fromRegion)!! - size)
        army?.sizePerRegion?.put(toRegion, army?.sizePerRegion?.get(toRegion)!! + size)
    }

    fun createAlliance(name : String) {
        Game.addAlliance(this, name)
    }

    fun makeProposal(alliance: Alliance, target : Player, proposalType: ProposalEnum) {
        when (proposalType) {
            ProposalEnum.KICK -> {
                val kickProposal = KickProposal(alliance = alliance,
                    target = target, initiator = this)
                alliance.proposalsList.add(kickProposal)
            }

            ProposalEnum.JOIN -> {
                val joinProposal = JoinProposal(alliance = alliance,
                    target = target, initiator = this)
                alliance.proposalsList.add(joinProposal)
            }
        }
    }
}