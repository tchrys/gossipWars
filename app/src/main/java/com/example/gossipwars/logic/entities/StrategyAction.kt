package com.example.gossipwars.logic.entities

import java.util.*

class StrategyAction(val initiatorId: UUID, val targetId : UUID, val targetRegion : Int,
                     val helpers : List<UUID>, val proposalEnum: ProposalEnum) : Action(initiatorId) {

}