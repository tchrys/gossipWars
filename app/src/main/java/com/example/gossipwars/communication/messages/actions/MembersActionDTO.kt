package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable
import java.util.*

class MembersActionDTO(val initiatorId : UUID, val targetId : UUID, val allianceId : UUID,
                       val proposalEnum: ProposalEnum
) : Serializable {

    fun convertToEntity(): MembersAction =
        MembersAction(
            GameHelper.findPlayerByUUID(initiatorId),
            GameHelper.findPlayerByUUID(targetId),
            GameHelper.findAllianceByUUID(allianceId),
            proposalEnum
        )
}