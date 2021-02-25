package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.AllianceDTO
import com.example.gossipwars.communication.messages.Message
import java.util.*

data class Alliance(val id: UUID) {

    lateinit var name: String
    var playersInvolved: MutableList<Player> = mutableListOf()
    lateinit var founderId: UUID;
    val messageList: MutableList<Message> = mutableListOf()
    val proposalsList : MutableList<Proposal> = mutableListOf()


    companion object Factory {
        fun initAlliance(player : Player, name : String) : Alliance {
            var alliance = Alliance(UUID.randomUUID())
            alliance.addPlayer(player)
            alliance.name = name
            var founderId = player.id
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

    fun convertToAllianceDTO() : AllianceDTO {
        return AllianceDTO(name, id, founderId, playersInvolved.map { player -> player.id }.toMutableList())
    }

}