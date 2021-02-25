package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Action
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.ProposalEnum
import com.example.gossipwars.logic.entities.StrategyAction
import java.io.Serializable
import java.util.*

class StrategyActionDTO(val initiatorId: UUID, val targetId : UUID, val targetRegion : Int,
                        val helpers : List<UUID>, val proposalEnum: ProposalEnum) :
            Action(initiatorId), Serializable {

    fun convertToStrategyAction(): StrategyAction =
        StrategyAction(initiator = Game.findPlayerByUUID(initiatorId),
                        target = Game.findPlayerByUUID(targetId),
                        targetRegion = targetRegion, proposalEnum = proposalEnum,
                        helpers = helpers.map { uuid -> Game.findPlayerByUUID(uuid) })
}