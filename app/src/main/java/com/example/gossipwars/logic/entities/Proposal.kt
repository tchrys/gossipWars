package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.JoinKickProposalDTO
import com.example.gossipwars.communication.messages.MembersAction
import com.example.gossipwars.communication.messages.ProposalResponse
import java.util.*

abstract class Proposal(open val alliance: Alliance, open val target : Player,
                        open val initiator : Player,
                        open var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                        open val proposalEnum: ProposalEnum, open val proposalId: UUID) {

    abstract fun allPlayersVoted() : Boolean
    abstract fun voteResult() : Boolean
    abstract fun createAction(): MembersAction
    abstract fun proposalAccepted(): Boolean
    abstract fun convertToJoinKickProposalDTO(): JoinKickProposalDTO

    fun sendMyVote(myVote: Boolean): ProposalResponse {
        return ProposalResponse(alliance.id, proposalId, myVote, Game.myId)
    }

    fun registerVote(player: Player, playerVote: Boolean) {
        votes[player] = playerVote
        if (proposalAccepted()) {
            Game.sendMembersAction(createAction())
        }
    }

}