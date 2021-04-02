package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable
import java.util.*

class StrategyActionDTO(
    private val initiatorId: UUID, private val targetId: UUID, private val targetRegion: Int,
    private val helpers: List<UUID>, val proposalEnum: ProposalEnum
) : Serializable {

    fun convertToEntity(): StrategyAction =
        StrategyAction(
            initiator = GameHelper.findPlayerByUUID(initiatorId),
            target = GameHelper.findPlayerByUUID(targetId),
            targetRegion = targetRegion,
            proposalEnum = proposalEnum,
            helpers = helpers.map { uuid -> GameHelper.findPlayerByUUID(uuid) })
}