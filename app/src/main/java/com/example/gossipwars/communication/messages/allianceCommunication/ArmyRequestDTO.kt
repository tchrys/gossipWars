package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ArmyRequest
import java.io.Serializable
import java.util.*

data class ArmyRequestDTO(val initiatorId: UUID,
                          val approverId: UUID, val armyOption: ArmyOption,
                          val increase: Int, val id: UUID): Serializable {

    fun convertToEntity(): ArmyRequest = ArmyRequest(id = id, initiator = GameHelper.findPlayerByUUID(initiatorId),
                                                    approver = GameHelper.findPlayerByUUID(approverId),
                                                    armyOption = armyOption, increase = increase)
}