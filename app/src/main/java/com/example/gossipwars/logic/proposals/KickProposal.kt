package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.allianceCommunication.JoinKickProposalDTO
import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import java.util.*

class KickProposal @JvmOverloads constructor(override val alliance: Alliance,
                                             override val target: Player, override val initiator : Player,
                                             override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                                             override val proposalEnum: ProposalEnum = ProposalEnum.KICK,
                                             override val proposalId: UUID,
                                             override var actionSent: Boolean = false) :
    Proposal(alliance, target, initiator, votes, proposalEnum, proposalId, actionSent) {

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size + 1

    override fun voteResult(): Boolean {
        val noVotes : Int = votes.size
        var yesVotes = 0
        for (v in votes.values) {
            if (v) yesVotes++
        }
        return yesVotes > noVotes / 2
    }

    override fun createAction() : MembersAction =
        MembersAction(
            initiator,
            target,
            alliance,
            ProposalEnum.KICK
        )

    override fun proposalAccepted() : Boolean = voteResult()

    fun actionTaken() {
        alliance.kickPlayer(target)
        alliance.proposalsList.remove(this)
    }

    fun convertToDTO(): JoinKickProposalDTO =
        JoinKickProposalDTO(
            alliance.id, target.id,
            initiator.id, proposalId,
            ProposalEnum.KICK
        )
}