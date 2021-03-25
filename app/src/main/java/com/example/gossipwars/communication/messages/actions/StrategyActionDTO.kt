package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable
import java.util.*

class StrategyActionDTO(
    val initiatorId: UUID, val targetId: UUID, val targetRegion: Int,
    val helpers: List<UUID>, val proposalEnum: ProposalEnum
) :
    Action(initiatorId), Serializable {

    fun convertToStrategyAction(): StrategyAction =
        StrategyAction(
            initiator = GameHelper.findPlayerByUUID(initiatorId),
            target = GameHelper.findPlayerByUUID(targetId),
            targetRegion = targetRegion,
            proposalEnum = proposalEnum,
            helpers = helpers.map { uuid -> GameHelper.findPlayerByUUID(uuid) })
}