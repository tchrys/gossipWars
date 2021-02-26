package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable
import java.util.*

class MembersActionDTO(val initiatorId : UUID, val targetId : UUID, val allianceId : UUID,
                       val proposalEnum: ProposalEnum
) : Action(initiatorId), Serializable {

    fun convertToEntity(): MembersAction =
        MembersAction(
            Game.findPlayerByUUID(initiatorId),
            Game.findPlayerByUUID(targetId),
            Game.findAllianceByUUID(allianceId),
            proposalEnum
        )
}