package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.actions.StrategyActionDTO
import com.example.gossipwars.communication.messages.allianceCommunication.StrategyProposalDTO
import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import java.util.*

class StrategyProposal(override val alliance: Alliance, override val target: Player,
                       override val initiator : Player,
                       override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                       var targetRegion : Int,
                       override val proposalEnum: ProposalEnum, override val proposalId: UUID) :
            Proposal(alliance, target, initiator, votes, proposalEnum, proposalId) {
    init {
        votes[initiator] = true
    }

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size + 1
    override fun voteResult(): Boolean = true

    fun getVoteFromPlayer(player: Player, vote : Boolean) {
        votes[player] = vote;
    }

    fun votesToList() = votes.filter { entry -> entry.value  }.toList().map { pair -> pair.first.id }

    override fun createAction() : Action =
        StrategyActionDTO(
            initiator.id, target.id, targetRegion,
            votesToList(), proposalEnum
        )

    override fun proposalAccepted(): Boolean = true

    fun convertToDTO(): StrategyProposalDTO =
        StrategyProposalDTO(
            alliance.id, target.id, initiator.id,
            targetRegion, proposalEnum, proposalId
        )

}