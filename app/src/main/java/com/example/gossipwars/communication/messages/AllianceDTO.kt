package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import java.io.Serializable
import java.util.*

class AllianceDTO(val name: String, val id: UUID, val founderId: UUID,
                    var playersInvolved: MutableList<UUID> = mutableListOf()) : Serializable {

    fun convertToAlliance(): Alliance {
        var alliance = Alliance(id)
        alliance.name = name
        alliance.founderId = founderId
        alliance.playersInvolved = playersInvolved.map { uuid -> Game.convertUUIDToPlayer(uuid) }.toMutableList()
        return alliance
    }
}