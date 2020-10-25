package com.example.gossipwars.logic.entities

import java.util.*

data class Alliance(val id: UUID) {
    var name: String? = null
    var playersInvolved: MutableList<Player> = mutableListOf()
    val messageList: MutableList<Message> = mutableListOf()
    val proposalsList : MutableList<Proposal> = mutableListOf()

    companion object Factory {
        fun initAlliance(player : Player, name : String) : Alliance {
            var alliance = Alliance(UUID.randomUUID())
            alliance.addPlayer(player)
            alliance.name = name
            return alliance
        }
    }

    fun addMessage(message : Message) {
        messageList.add(message)
    }

    fun addPlayer(player : Player) {
        playersInvolved.add(player)
        player.joinAlliance(this)
    }

    fun kickPlayer(player: Player) {
        playersInvolved.remove(player)
        player.quitAlliance(this)
    }

    fun allianceSize() : Int = playersInvolved.size

}