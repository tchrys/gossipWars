package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ArmyRequest
import java.io.Serializable
import java.util.*

data class ArmyRequestDTO(val initiatorId: UUID, val approverId: UUID, val armyOption: ArmyOption,
                          val increase: Int): Serializable {

    fun convertToEntity(): ArmyRequest = ArmyRequest(initiator = Game.findPlayerByUUID(initiatorId),
                                                    approver = Game.findPlayerByUUID(approverId),
                                                    armyOption = armyOption, increase = increase)
}