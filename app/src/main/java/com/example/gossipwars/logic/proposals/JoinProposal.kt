package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.allianceCommunication.JoinKickProposalDTO
import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import java.util.*

class JoinProposal @JvmOverloads
    constructor(override val alliance: Alliance, override val target: Player,
                override val initiator : Player,
                override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                override val proposalEnum: ProposalEnum = ProposalEnum.JOIN,
                override val proposalId: UUID) :
        Proposal(alliance, target, initiator, votes, proposalEnum, proposalId) {

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size

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

    override fun createAction() : MembersActionDTO =
        MembersActionDTO(
            initiator.id,
            target.id,
            alliance.id,
            ProposalEnum.JOIN
        )

    override fun proposalAccepted() : Boolean = allPlayersVoted() && voteResult()

    fun actionTaken() {
        alliance.addPlayer(target)
        alliance.proposalsList.remove(this)
    }

    fun convertToDTO(): JoinKickProposalDTO =
        JoinKickProposalDTO(
            alliance.id, target.id,
            initiator.id, proposalId,
            ProposalEnum.JOIN
        )
}