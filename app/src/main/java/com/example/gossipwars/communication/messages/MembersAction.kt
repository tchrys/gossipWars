package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Action
import com.example.gossipwars.logic.entities.ProposalEnum
import java.util.*

class MembersAction(private val initiatorId : UUID, val targetId : UUID, val allianceId : UUID,
                        val proposalEnum: ProposalEnum
) : Action(initiatorId) {
    fun getInitiatorId(): UUID = initiatorId
}