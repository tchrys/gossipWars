package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.*
import java.io.Serializable
import java.util.*

class StrategyProposalDTO(val allianceId: UUID, val target: UUID, val initiator: UUID,
                          val targetRegion: Int, val proposalEnum: ProposalEnum,
                          val proposalId: UUID) : Serializable {

    fun convertToStrategyProposal(): StrategyProposal =
        StrategyProposal(alliance = Game.findAllianceByUUID(allianceId),
                        initiator = Game.findPlayerByUUID(initiator),
                        target = Game.findPlayerByUUID(target),
                        proposalId = proposalId, proposalEnum = proposalEnum, targetRegion = targetRegion)
}