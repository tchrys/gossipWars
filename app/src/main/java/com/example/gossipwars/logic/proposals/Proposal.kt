package com.example.gossipwars.logic.proposals

import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.communication.messages.actions.StrategyActionDTO
import com.example.gossipwars.communication.messages.allianceCommunication.ProposalResponse
import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*

abstract class Proposal(
    open val alliance: Alliance, open val target: Player,
    open val initiator: Player,
    open var votes: MutableMap<Player, Boolean> = mutableMapOf(),
    open val proposalEnum: ProposalEnum, open val proposalId: UUID,
    open var actionSent: Boolean = false
) : Serializable {

    abstract fun allPlayersVoted(): Boolean
    abstract fun voteResult(): Boolean
    abstract fun createAction(): Action
    abstract fun proposalAccepted(): Boolean

    fun sendMyVote(myVote: Boolean): ProposalResponse {
        return ProposalResponse(
            alliance.id,
            proposalId,
            myVote,
            Game.myId
        )
    }

    fun isMemberProposal(): Boolean =
        ProposalEnum.JOIN == proposalEnum || ProposalEnum.KICK == proposalEnum

    fun registerVote(player: Player, playerVote: Boolean) {
        votes[player] = playerVote
        if (!actionSent && proposalAccepted() && isMemberProposal()) {
            actionSent = true
            GlobalScope.launch { Game.sendMembersAction(createAction() as MembersAction) }
        }
    }

}