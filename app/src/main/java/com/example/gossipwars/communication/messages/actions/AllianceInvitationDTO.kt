package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import java.io.Serializable
import java.util.*

class AllianceInvitationDTO(val name: String, val id: UUID, val founderId: UUID,
                            var playersInvolved: MutableList<UUID> = mutableListOf()) : Serializable {

    fun convertToEntity(): Alliance {
        var alliance = Alliance(id)
        alliance.name = name
        alliance.founderId = founderId
        alliance.playersInvolved = playersInvolved.map { uuid -> Game.findPlayerByUUID(uuid) }.toMutableList()
        return alliance
    }
}