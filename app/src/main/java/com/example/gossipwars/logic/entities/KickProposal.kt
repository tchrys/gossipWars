package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.JoinKickProposalDTO
import com.example.gossipwars.communication.messages.MembersAction
import java.util.*

class KickProposal @JvmOverloads constructor(override val alliance: Alliance,
    override val target: Player, override val initiator : Player,
    override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
    override val proposalEnum: ProposalEnum = ProposalEnum.KICK, override val proposalId: UUID) :
    Proposal(alliance, target, initiator, votes, proposalEnum, proposalId) {

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size + 1

    override fun voteResult(): Boolean {
        if (allPlayersVoted())
            return false
        val noVotes : Int = votes.size
        var yesVotes : Int = 0
        for (v in votes.values) {
            if (v) yesVotes++
        }
        return yesVotes > noVotes / 2
    }

    override fun createAction() : MembersAction =
        MembersAction(
            initiator.id,
            target.id,
            alliance.id,
            ProposalEnum.KICK
        )

    override fun proposalAccepted() : Boolean = allPlayersVoted() && voteResult()

    fun actionTaken() {
        alliance.kickPlayer(target)
        alliance.proposalsList.remove(this)
    }

    fun convertToJoinKickProposalDTO(): JoinKickProposalDTO {
        return JoinKickProposalDTO(alliance.id, target.id, initiator.id, proposalId, ProposalEnum.KICK)
    }

}