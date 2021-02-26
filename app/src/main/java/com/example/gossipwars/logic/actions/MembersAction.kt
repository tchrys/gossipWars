package com.example.gossipwars.logic.actions

import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable

class MembersAction(private val initiator : Player, val target : Player, val alliance : Alliance,
                    val proposalEnum: ProposalEnum
) : Serializable {

    fun convertToDTO(): MembersActionDTO =
        MembersActionDTO(
            initiator.id, target.id, alliance.id,
            proposalEnum
        )
}