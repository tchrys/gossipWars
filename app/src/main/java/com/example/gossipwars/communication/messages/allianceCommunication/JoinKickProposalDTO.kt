package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.JoinProposal
import com.example.gossipwars.logic.proposals.KickProposal
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable
import java.util.*

class JoinKickProposalDTO(
    val allianceId: UUID, val target: UUID, val initiator: UUID,
    val proposalId: UUID, val proposalEnum: ProposalEnum
) : Serializable {

    fun convertToEntity(): Proposal? {
        return when (proposalEnum) {
            ProposalEnum.JOIN -> JoinProposal(
                target = GameHelper.findPlayerByUUID(target),
                proposalEnum = proposalEnum,
                proposalId = proposalId,
                initiator = GameHelper.findPlayerByUUID(initiator),
                alliance = GameHelper.findAllianceByUUID(allianceId)
            )
            ProposalEnum.KICK -> KickProposal(
                target = GameHelper.findPlayerByUUID(target),
                proposalEnum = proposalEnum,
                proposalId = proposalId,
                initiator = GameHelper.findPlayerByUUID(initiator),
                alliance = GameHelper.findAllianceByUUID(allianceId)
            )
            else -> null
        }
    }
}