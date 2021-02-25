package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.*
import java.io.Serializable
import java.util.*

class JoinKickProposalDTO(val allianceId: UUID, val target: UUID, val initiator: UUID,
                          val proposalId: UUID, val proposalEnum: ProposalEnum) : Serializable {

    fun convertToJoinOrKickProposal(): Proposal? {
        return if (ProposalEnum.JOIN == proposalEnum) {
            JoinProposal(target = Game.findPlayerByUUID(target),
                proposalEnum = proposalEnum, proposalId = proposalId,
                initiator = Game.findPlayerByUUID(initiator), alliance = Game.findAllianceByUUID(allianceId))
        } else if (ProposalEnum.KICK == proposalEnum) {
            KickProposal(target = Game.findPlayerByUUID(target), proposalEnum = proposalEnum,
                proposalId = proposalId, initiator = Game.findPlayerByUUID(initiator), alliance = Game.findAllianceByUUID(allianceId))
        } else {
            null
        }
    }
}