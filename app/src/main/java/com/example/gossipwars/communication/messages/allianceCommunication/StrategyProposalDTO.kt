package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.example.gossipwars.logic.proposals.StrategyProposal
import java.io.Serializable
import java.util.*

class StrategyProposalDTO(val allianceId: UUID, val target: UUID, val initiator: UUID,
                          val targetRegion: Int, val proposalEnum: ProposalEnum,
                          val proposalId: UUID) : Serializable {

    fun convertToEntity(): StrategyProposal =
        StrategyProposal(
            alliance = Game.findAllianceByUUID(allianceId),
            initiator = Game.findPlayerByUUID(initiator),
            target = Game.findPlayerByUUID(target),
            proposalId = proposalId,
            proposalEnum = proposalEnum,
            targetRegion = targetRegion
        )
}