package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Action
import com.example.gossipwars.logic.entities.ProposalEnum
import java.util.*

class StrategyAction(private val initiatorId: UUID, val targetId : UUID, val targetRegion : Int,
                     val helpers : List<UUID>, val proposalEnum: ProposalEnum) : Action(initiatorId) {
    fun getInitiatorId(): UUID = initiatorId
}