package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.StrategyAction
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

    fun votesToList() =
        votes.filter { entry -> entry.value  }.toList().map { pair -> pair.first.id }

    fun createAction() : StrategyAction =
        StrategyAction(
            initiator.id,
            target.id,
            targetRegion,
            votesToList(),
            proposalEnum
        )
}