package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.allianceCommunication.ArmyRequestDTO
import com.example.gossipwars.logic.entities.Player
import java.util.*

data class ArmyRequest(val initiator: Player, val approver: Player, val armyOption: ArmyOption,
                       val increase: Int, val id: UUID = UUID.randomUUID()) {

    fun convertToDTO(): ArmyRequestDTO = ArmyRequestDTO(initiator.id, approver.id, armyOption, increase, id)
}