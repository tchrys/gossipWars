package com.example.gossipwars.logic.entities

import java.util.*

class MembersAction(val initiatorId : UUID, val targetId : UUID, val allianceId : UUID,
                        val proposalEnum: ProposalEnum) : Action(initiatorId) {
}