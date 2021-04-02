package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.GameHelper
import java.io.Serializable
import java.util.*

class AllianceInvitationDTO(
    val name: String, val id: UUID, private val founderId: UUID,
    var playersInvolved: MutableList<UUID> = mutableListOf()
) : Serializable {

    fun convertToEntity(): Alliance = Alliance(id).apply {
        this.name = this@AllianceInvitationDTO.name
        this.founderId = this@AllianceInvitationDTO.founderId
        this.playersInvolved = this@AllianceInvitationDTO.playersInvolved.map { uuid ->
            GameHelper.findPlayerByUUID(uuid)
        }.toMutableList()
    }
}