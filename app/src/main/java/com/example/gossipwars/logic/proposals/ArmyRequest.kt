package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.allianceCommunication.ArmyRequestDTO
import com.example.gossipwars.logic.entities.Player

data class ArmyRequest(val initiator: Player, val approver: Player, val armyOption: ArmyOption,
                       val increase: Int) {

    fun convertToDTO(): ArmyRequestDTO = ArmyRequestDTO(initiator.id, approver.id, armyOption, increase)
}